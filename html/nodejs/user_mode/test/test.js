$(document).ready(function(){
  const theForm = $("#the_form");
  theForm.submit(evt=>{
    evt.preventDefault();
    handleFormSubmit(theForm);
  });
});

function handleFormSubmit(theForm){
  $.post( '/api/',
           theForm.serialize(),
           onFormSubmitComplete,
           'json').fail(()=>alert("Failed to submit form..."));
}

function onFormSubmitComplete(response){
  console.log(response);
  alert("Thank you for submitting your information!");
}