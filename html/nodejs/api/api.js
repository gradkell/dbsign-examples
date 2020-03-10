/*
*************************************************************************
* This code is provided 'as is' with no warranty expressed or implied.  *
*                                                                       *
* It is not provided or distributed as part of the DBsign Data Security *
* Suite, nor is it officially supported by Gradkell Systems, Inc.       *
*                                                                       *
* It is provided solely for demonstration purposes.                     *
*************************************************************************
*/

const http = require('http');
const qs = require('querystring');
const url = require('url');
const dbsign = require('./dbsign.js');

/* Create a server on port 8888 - You should set up a ReverseProxy on 
   your web server to make this server publically available. */
const server = http.createServer((request, response)=>{
  let reqFunction = null;

  if(request.method==='GET') reqFunction = handleGet;
  else if(request.method==='POST') reqFunction = handlePost;
  else reqFunction = handleUnsupportedMethod;

  reqFunction(request, response);
}).listen(8888);

/* responseData is a plain object [name-value pairs].
   The response will be the JSON representation of that object. 
   All response data is written through this function. */
function processResponse(response, responseData){
  response.writeHead(200, {'Content-Type':'application/json'});
  response.end(JSON.stringify(responseData));
}

/* Parse the query string and use it as the response. */
function handleGet(request, response){
  const submittedData = url.parse(request.url, true).query;
  processResponse(response, {
    submitted_data:submittedData
  });
}

/* Parse the posted data.  Verify the signature in the data,
   and use the posted data and DBsign verification result as
   the response. */
function handlePost(request, response){
  let body = '';

  request.on('data', chunk=>body+=chunk );
  request.on('end', ()=>{
    const submittedData = qs.parse(body);

    const dbsSignature = submittedData.dbs_signature;
    /* Reconstruct the signed data. */
    const dbsDtbs = `${submittedData.fname} ${submittedData.lname}`;
    const dbsDtbsFmt = 'TEXT';

    /* Verify the signature. */
    dbsign.appVerify(dbsSignature, dbsDtbs, dbsDtbsFmt).then(resp=>{
      /* Verify request [http] was successful.  Respond with the verification result. */
      processResponse(response, {
        submitted_data:submittedData,
        dbs_verify_response:resp
      });
    }).catch(error=>{
      /* The verify request [http] failed.  Respond with the error message. */
      processResponse(response, {
        error_message:error.message
      });
    });
  });
}

/* Return an error message if the request was not a GET or POST */
function handleUnsupportedMethod(request, response){
  processResponse(response, {
    error_message:`Unsupported request method ${request.method}.`
  });
}
