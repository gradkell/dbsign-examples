const http = require('http');
const qs = require('querystring');
const url = require('url');
const dbsign = require('./dbsign.js');
const forge = require('node-forge');
const fs = require('fs');

const server = http.createServer((request, response)=>{
  let reqFunction = null;

  if(request.method==='GET') reqFunction = handleGet;
  else if(request.method==='POST') reqFunction = handlePost;
  else reqFunction = handleUnsupportedMethod;

  reqFunction(request, response);
}).listen(8888);

function processResponse(response, responseData){
  const buff = Buffer.from(responseData.dbs_pdf_sign_response.DBS_SIGNATURE, 'base64');

  response.writeHead(200, {'Content-Type':'application/pdf', 'Content-disposition':'attachment;filename=LoremIpsumForm_Signed.pdf'});
  response.end(buff);
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

    getClientCertSubjectDn(request);

    dbsign.pdfSign(
      getClientCertSubjectDn(request),
      getClientCertCommonName(request),
      getBase64Pdf(),
      "signatureOne",
      "Approving first and last name.",
      {
        firstName:submittedData.fname,
        lastName:submittedData.lname
      }).then(resp=>{
      processResponse(response, {
        submitted_data:submittedData,
        dbs_pdf_sign_response:resp
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

function getBase64Pdf() {
  const pdf = fs.readFileSync('LoremIpsumForm.pdf');
  const b64 = Buffer.from(pdf).toString('base64');
  return b64;
}

function getClientCert(request)
{
  const pem = `-----BEGIN CERTIFICATE-----${request.headers['x-arr-clientcert']}-----END CERTIFICATE-----`;
  const cert = forge.pki.certificateFromPem(pem);
  return cert;
}

function getClientCertSubjectDn(request)
{
  const cert = getClientCert(request);
  cert.subject.attributes.find(element=>element.type==='0.9.2342.19200300.100.1.1').shortName="UID";
  const subjectDn = cert.subject.attributes.reverse()
    .map(attr => [attr.shortName.toLowerCase(), attr.value].join('='))
    .join(',');

    return subjectDn;
}

function getClientCertCommonName(request)
{
  const cert = getClientCert(request);
  const cn = cert.subject.attributes.find(element=>element.shortName==='CN');

  return cn.value;
}