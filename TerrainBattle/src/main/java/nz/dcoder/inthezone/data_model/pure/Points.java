package nz.dcoder.inthezone.data_model.pure;

/**
 * Pairs up total and maximum points.  Used to represent hit point status,
 * action point status, movement point status, and possibly others.
 * */
public class Points {
	public final int max;
	public final int total;

	public Points(int max, int total) {
		this.max = max;
		this.total = total;
	}
}

