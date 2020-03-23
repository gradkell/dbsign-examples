const http = require('http');
const qs = require('querystring');

let _hostname = 'localhost';
let _port = '8080';
let _path = '/dbsign/server';

exports.initialize = (hostname, port, path) => {
  _hostname = hostname;
  _port = port;
  _path = path;
};

exports.appSign = (userId, dtbs, dtbsFmt) =>{
  return _doDBsignRequest({
    'CONTENT_TYPE':'APP_SIGN_SAS',
    'DBS_SAS_AUTH_TYPE':'USERID',
    'DBS_SAS_AUTH_VALUE':userId,
    'DBS_DTBS':dtbs,
    'DBS_DTBS_FMT':dtbsFmt,
    'DBS_RETURN_SIGNER_INFO':true
  });
}

exports.appVerify = (signature, dtbs, dtbsFmt) => {
  return _doDBsignRequest({
    'CONTENT_TYPE':'APP_VERIFY',
    'DBS_SIGNATURE':signature,
    'DBS_DTBS':dtbs,
    'DBS_DTBS_FMT':dtbsFmt
  });
}

let _doDBsignRequest = (parameters) => {
  return new Promise((resolve, reject)=>{
    _doHttpPost(parameters).then(response=>{
      const obj = qs.parse(response.text);
      resolve(obj);
    }).catch(error=>reject(error));
  });
}

let _doHttpPost = (data) => {
  return new Promise((resolve, reject)=>{
    const options = {
      hostname:_hostname,
      port:_port,
      path:_path,
      method:'POST'
    };
  
    const req = http.request(options, res=>{
      res.text = '';
      res.on('data', chunk=>res.text+=chunk.toString() );
      res.on('end', ()=>resolve(res));
    }).on('error', error=>reject(error));

    if( !(typeof data==='string') ) data = qs.stringify(data);
    req.write(data);
    req.end();
  });
}