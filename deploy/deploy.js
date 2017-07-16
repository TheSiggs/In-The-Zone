'use strict'

const http = require('http')
const fs = require('fs')
const spawn = require('child_process').spawn
const log = console.log
const ERR = console.error

process.env.TZ = 'Pacific/Auckland'
log(`${new Date()} - PID - ${process.pid}`)

process.on('uncaughtException', function(err){
  log(`${new Date()} - END by ERR - ${process.pid}`)
  ERR(new Date()+' uncaughtException:')
  ERR(err)
  process.exit(1)
})


var server = http.createServer(srv)
server.listen(4010)

function srv(req, res){

  log(req.url)

  if(req.url=='/b9b81349ba29f5b8f8c684c822040b1a'){ //b9b81349ba29f5b8f8c684c822040b1a
    let proc = spawn('./GitLabHook.sh')
    proc.stderr.on("data", ERR)
    proc.stdout.on("data", log)
    return
  }


  let url = req.url.split('/')
  if(url[1]=='client'){
    if(url[2] == null || url[2] == ''){
      res.writeHead(200)
      res.end(clientsHTML())
      return
    }
    if(clients[url[2]]){
      srvFile(res, url[2])
      return
    }
  }

  res.writeHead(404)
  res.end()

}


var clients = {}

function clientsHTML(){
  clients['client-latest.jar'] = true
  return `
  <html>
    <head>
      <title>clients</title>
    </head>
    <body>

     <ul><li>
        <a href="./client/client-latest.jar">Latest</a>
      </li></ukl>

    <body>
  </html>
`

}

clientsHTML()


function srvFile(res, file){
  log('srvFile/'+file)
  fs.readFile('.' + file, function(err, b) {
    if (err) {
      res.writeHead(404)
      res.end()
      return
    } else {
      res.writeHead(200)
      res.end(b)
      return
    }
  })

}



















