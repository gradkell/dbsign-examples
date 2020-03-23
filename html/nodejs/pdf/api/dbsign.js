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

exports.pdfSign = (userId, friendlyUserId, base64Pdf, pdfSigFieldName, reason, fields) => {
  const requestData = {
    'CONTENT_TYPE':'PDF_SIGN',
    'DBS_SAS_AUTH_TYPE':'USERID',
    'DBS_SAS_AUTH_VALUE':userId,
    'DBS_PDF_SIGNER_NAME':friendlyUserId,
    'DBS_DTBS':base64Pdf,
    'DBS_DTBS_FMT':'BASE64',
    'DBS_PDF_SIG_FIELD_NAME':pdfSigFieldName,
    'DBS_PDF_SIGNER_REASON':reason,
    'DBS_SHOW_SIGNER_REASON':false,
    'DBS_SHOW_SIGN_DATE':true,
    'DBS_LOCK_FILLED_FIELDS':true,
    'DBS_SIGN_DATE_TIMEZONE':'America/New_York',
    'DBS_SIGN_DATE_FORMAT':'MMM d, yyyy HH:mm:ss zz'
  }
  
  let fieldList = "";
  for( const key in fields ) {
    requestData[key] = fields[key];

    if(fieldList.length>0) fieldList += ',';
    fieldList += key;
  }

  requestData['DBS_FILL_FIELDS'] = fieldList;

  return _doDBsignRequest(requestData);
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