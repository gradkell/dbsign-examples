const http = require('http');
const qs = require('querystring');
const url = require('url');
const dbsign = require('./dbsign.js');

const server = http.createServer((request, response)=>{
  let reqFunction = null;

  if(request.method==='GET') reqFunction = handleGet;
  else if(request.method==='POST') reqFunction = handlePost;
  else reqFunction = handleUnsupportedMethod;

  reqFunction(request, response);
}).listen(8888);

function processResponse(response, responseData){
  response.writeHead(200, {'Content-Type':'application/json'});
  response.end(JSON.stringify(responseData));
}

function handleGet(request, response){
  const submittedData = url.parse(request.url, true).query;
  processResponse(response, {
    submitted_data:submittedData
  });
}

function handlePost(request, response){
  let body = '';

  request.on('data', chunk=>body+=chunk );
  request.on('end', ()=>{
    const submittedData = qs.parse(body);

    dbsign.appSign('bob1234', `${submittedData.fname} ${submittedData.lname}`, 'TEXT').then(resp=>{
      processResponse(response, {
        submitted_data:submittedData,
        dbs_app_sign_response:resp
      });
    }).catch(error=>{
      processResponse(response, {
        error_message:error.message
      });
    });

    
  });
}

function handleUnsupportedMethod(request, response){
  processResponse(response, {
    error_message:`Unsupported request method ${request.method}.`
  });
}
