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

/* EXAMPLE USAGE:
 
const dbsign = require('./dbsign.js');
dbsign.initialize('localhost', 8080, '/dbsign/server');

dbsign.appVerify('BASE64_ENCODED_SIGNATURE', 'My dog has fleas.', 'TEXT').then(response=>{
  console.log('response', response);
}).catch(error=>{
  console.log('error', error);
});

*/

/* This example uses HTTP, use the 'https' module if you need SSL/TLS. */
const http = require('http');
const qs = require('querystring');

/* Default location of the DBsign server -- http://localhost:8080/dbsign/server */
let _hostname = 'localhost';
let _port = 8080;
let _path = '/dbsign/server';

/* Only need to call this function if not using the default server URL above. */
exports.initialize = (hostname, port, path) => {
  _hostname = hostname;
  _port = port;
  _path = path;
};

/* signature MUST be Base64 encoded CMS/PKCS7 */
exports.appVerify = (signature, dtbs, dtbsFmt) => {
  return _doDBsignRequest({
    'CONTENT_TYPE':'APP_VERIFY',
    'DBS_SIGNATURE':signature,
    'DBS_DTBS':dtbs,
    'DBS_DTBS_FMT':dtbsFmt
  });
}

/* Functions below are NOT exported. */

/* Generic DBsign request function that wraps _doHttpPost. 
   Returned Promise provides the parsed DBsign server response
   when successfully completed. */
let _doDBsignRequest = (parameters) => {
  return new Promise((resolve, reject)=>{
    _doHttpPost(parameters).then(response=>{
      const obj = qs.parse(response.text); // URL decode the server's response.
      resolve(obj);
    }).catch(error=>reject(error));
  });
}

/* */
let _doHttpPost = (data) => {
  return new Promise((resolve, reject)=>{
    const options = {
      hostname:_hostname,
      port:_port,
      path:_path,
      method:'POST'
    };
  
    /* Resolve the promise with the response object, or reject on error.
       The response object will have a new member called 'text' containing
       the text of the response from the server. */
    const req = http.request(options, res=>{
      res.text = '';
      res.on('data', chunk=>res.text+=chunk.toString() );
      res.on('end', ()=>resolve(res));
    }).on('error', error=>reject(error));

    /* URL encode the post data if not already a string. */
    if( !(typeof data==='string') ) data = qs.stringify(data);
    req.write(data);
    req.end();
  });
}