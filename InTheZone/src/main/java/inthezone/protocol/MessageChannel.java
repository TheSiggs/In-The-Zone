package inthezone.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

/**
 * An asynchronous channel for Messages.
 * */
public class MessageChannel {
	private final SocketChannel channel;
	private SelectionKey skey;

	private final Queue<SendState> sendQueue = new LinkedList<>();
	
	private final ByteBuffer sndBuffer = ByteBuffer.allocate(8 * 1024);
	private final ByteBuffer recBuffer = ByteBuffer.allocate(32 * 1024);
	private final CharBuffer msgBuffer = CharBuffer.allocate(8 * 1024);

	private final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
	private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();

	/**
	 * @param channel the underlying SocketChannel
	 * @param sel the Selector for non-blocking IO
	 * @param key the selector key for this channel
	 * */
	public MessageChannel(
		final SocketChannel channel, final Selector sel, final Object key
	)
		throws IOException, ClosedChannelException
	{
		this.channel = channel;

		channel.configureBlocking(false);
		this.skey = channel.register(sel, SelectionKey.OP_READ, key);

		encoder.reset();
		decoder.reset();
	}

	/**
	 * Reset the selector key to read only mode.  Call after all send operations
	 * are complete.
	 * @param sel the Selector for non-blocking IO
	 * @param key the selector key for this channel.
	 * */
	public void resetSelector(final Selector sel, final Object key)
		throws ClosedChannelException
	{
		this.skey = channel.register(sel, SelectionKey.OP_READ, key);
	}

	/**
	 * Associate this channel with a new selector key.
	 * */
	public void affiliate(final Object key) {
		skey.attach(key);
	}

	/**
	 * Request to send a message.  The message will be send when the channel is
	 * ready for writing.
	 * */
	public void requestSend(final Message msg) {
		skey.interestOps(skey.interestOps() | SelectionKey.OP_WRITE);
		sendQueue.add(new SendState(CharBuffer.wrap(msg.toString())));
	}

	/**
	 * Read messages (non-blocking).
	 * @return As many complete messages as can be read without blocking.
	 * Incomplete messages may be left in the buffer until the next call to
	 * doRead.
	 * @throws IOException not recoverable. The connection should be terminated.
	 * */
	public List<Message> doRead() throws IOException, ProtocolException {
		final int n = channel.read(recBuffer);
		if (n == -1) {
			channel.close();
			recBuffer.clear();
			msgBuffer.clear();
			sndBuffer.clear();
			sendQueue.clear();
			decoder.reset();
			encoder.reset();
			return new LinkedList<Message>();
		}

		recBuffer.flip();
		msgBuffer.mark();
		final CoderResult r = decoder.decode(recBuffer, msgBuffer, false);
		recBuffer.compact();

		if (r.isOverflow()) {
			throw new IOException("Overlong message");
		} else if (r.isError()) {
			throw new IOException("Character set error (please use UTF-8)");
		}

		final List<Message> msgs = new LinkedList<>();
		Optional<Message> msg = readMessage(msgBuffer);
		while (msg.isPresent()) {
			msgs.add(msg.get());
			msg = readMessage(msgBuffer);
		}

		return msgs;
	}

	/**
	 * Attempt to read a message out of a character buffer.  The mark must be set
	 * to the position to start checking from. The position must be set to the
	 * place where the next write to the buffer would go.  If it returns nothing,
	 * then the buffer is reset to its initial state.  If it returns a message,
	 * then the buffer is compacted and the mark is reset to 0 in preparation for
	 * the next readMessage call.
	 *
	 * Assumes the limit is equal to the capacity.
	 * */
	private Optional<Message> readMessage(final CharBuffer msgBuffer)
		throws ProtocolException
	{
		String msg = null;
		msgBuffer.limit(msgBuffer.position()).reset();
		while (msgBuffer.hasRemaining()) {
			char c = msgBuffer.get();
			if (c == '\n') {
				int max = msgBuffer.limit();
				msgBuffer.flip();
				msg = msgBuffer.toString();
				msgBuffer.position(msgBuffer.limit()).limit(max);
				msgBuffer.compact();
				break;
			}
		}

		if (msg == null) {
			msgBuffer.position(msgBuffer.limit());
			msgBuffer.limit(msgBuffer.capacity());
			return Optional.empty();
		} else {
			final int t = msgBuffer.position();
			msgBuffer.position(0).mark().position(t);
			return Optional.of(Message.fromString(msg));
		}
	}

	/**
	 * Write as many of the buffered messages as possible without blocking.
	 * */
	public void doWrite() throws IOException {
		// write as many messages as possible to the send buffer.
		while (writeMessage());

		sndBuffer.flip();
		channel.write(sndBuffer);
		sndBuffer.compact();

		if (sndBuffer.position() == 0 && sendQueue.isEmpty()) {
			skey.interestOps(skey.interestOps() & ~SelectionKey.OP_WRITE);
		}
	}

	/**
	 * Writes data from the message on the top of the queue to the send buffer.
	 * @return true if there is more data to write, false otherwise.
	 * */
	private boolean writeMessage() throws IOException {
		if (sendQueue.isEmpty()) return false;

		boolean moreToWrite = false;
		final SendState sending = sendQueue.element();

		if (sending.doEncode) {
			final CoderResult r = encoder.encode(sending.buffer, sndBuffer, false);
			if (r.isUnderflow()) {
				moreToWrite = true;
				sending.doEncode = false;
				sending.doFinalEncode = true;
			} if (r.isError()) {
				throw new IOException("Cannot encode character");
			}
		}

		if (sending.doFinalEncode) {
			final CoderResult r = encoder.encode(sending.buffer, sndBuffer, true);
			if (r.isUnderflow()) {
				moreToWrite = true;
				sending.doFinalEncode = false;
				sending.doFlush = true;
			} else if (r.isError()) {
				throw new IOException("Cannot encode character");
			}
		}

		if (sending.doFlush) {
			final CoderResult r = encoder.flush(sndBuffer);
			if (r.isUnderflow()) {
				moreToWrite = true;
				sending.doFlush = false;
				encoder.reset();
				sendQueue.remove();
			} else if (r.isError()) {
				throw new IOException("Error flushing output message buffer");
			}
		}

		return moreToWrite;
	}
}


/**
 * The state of the sending channel.
 * */
class SendState {
	public final CharBuffer buffer;
	public boolean doEncode = true;
	public boolean doFinalEncode = false;
	public boolean doFlush = false;

	public SendState(final CharBuffer buffer) {
		this.buffer = buffer;
	}
}

