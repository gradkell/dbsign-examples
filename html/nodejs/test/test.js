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

let dbsjsws = null;

$(document).ready(function(){
  const theForm = $("#the_form");
  theForm.submit(evt=>{
    evt.preventDefault();
    handleFormSubmit(theForm);
  });

  /* Initialize DBsignJSWS */
  dbsjsws = new CDBsignJSWS({
    mobile_license_id:'9E282C11B1FC9DD3ABDF275F973A1A19DDD7A5F9',
    data_url:'/dbsign/server',
    on_init_failure_callback:()=>alert('DBsign failed to initialize.'),
    on_ready_callback:onDBsignReady
  });

  dbsjsws.initialize();
});

function handleFormSubmit(theForm){
  dbsjsws.DBS_AppSign({
    dbs_dtbs:`${$('#fname').val()} ${$('#lname').val()}`,
    dbs_dtbs_fmt:'TEXT',
    callback:results=>{
      console.log(results);

      if(results['DBS_ERROR_INFO']['DBS_ERROR_VAL']!==0){
        alert('Error signing data.');
      } else {
        $('#dbs_signature').val(results['RESULT_INFO']['DBS_SIGNATURE']);
        $.post( '/api/',
            theForm.serialize(),
            onFormSubmitComplete,
            'json').fail(()=>alert("Failed to submit form..."));
      }
    }
  });
}

function onFormSubmitComplete(response){
  console.log(response);
  alert("Thank you for submitting your information!");
}

function onDBsignReady(){
  console.log(`DBsign v${dbsjsws.DBS_GetVersion()} loaded.`);
  $('#submit_btn').prop('disabled', false);
}