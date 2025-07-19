document.addEventListener('DOMContentLoaded', function() {
  const useSampleCheckbox = document.getElementById('useSample');
  const fileInput = document.getElementById('file');
  if (useSampleCheckbox && fileInput) {
    useSampleCheckbox.addEventListener('change', function() {
      if (useSampleCheckbox.checked) {
        fileInput.removeAttribute('required');
      } else {
        fileInput.setAttribute('required', 'required');
      }
    });
  }

  const form = document.querySelector('form');
  const spinnerOverlay = document.getElementById('spinner-overlay');
  if (form) {
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      spinnerOverlay.classList.add('is-active');
      const formData = new FormData(form);
      fetch(form.getAttribute('action') || window.location.pathname, {
        method: 'POST',
        body: formData,
        headers: {
          'X-Requested-With': 'XMLHttpRequest'
        }
      })
      .then(response => {
        if (response.headers.get('content-type') && response.headers.get('content-type').includes('application/json')) {
          response.json().then(data => {
            if (data.redirect) {
              window.location.href = data.redirect;
            }
          });
          return;
        }
        if (response.redirected) {
          window.location.href = response.url;
          return;
        }
        spinnerOverlay.classList.remove('is-active');
        if (response.ok) {
          alert('Upload successful!');
        } else {
          response.text().then(text => {
            showErrorModal(text || 'Upload failed.');
          });
        }
      })
      .catch(error => {
        spinnerOverlay.classList.remove('is-active');
        showErrorModal('Upload error: ' + error);
      });

    function showErrorModal(message) {
      var errorModal = document.getElementById('error-modal');
      var errorMsg = errorModal.querySelector('.modal-message');
      if (errorMsg) errorMsg.textContent = message;
      errorModal.classList.add('is-active');
    }
    });
  }

  const dropZone = document.getElementById('drop-zone');
  if (dropZone && fileInput) {
    dropZone.addEventListener('dragover', function(e) {
      e.preventDefault();
      dropZone.classList.add('has-background-link-light');
    });
    dropZone.addEventListener('dragleave', function(e) {
      e.preventDefault();
      dropZone.classList.remove('has-background-link-light');
    });
    dropZone.addEventListener('drop', function(e) {
      e.preventDefault();
      dropZone.classList.remove('has-background-link-light');
      if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
        fileInput.files = e.dataTransfer.files;
        showFileName(fileInput);
      }
    });
    fileInput.addEventListener('change', function() {
      showFileName(fileInput);
    });
  }

  const modal = document.getElementById('limit-modal');
  if (modal && modal.dataset.show === "true") {
    modal.classList.add('is-active');
  }

  const closeBtn = document.getElementById('close-modal-btn');
  if (closeBtn) {
    closeBtn.onclick = function() {
      modal.classList.remove('is-active');
    }
  }

  window.onclick = function(event) {
    if (event.target === modal) {
      modal.classList.remove('is-active');
    }
  }

  // Error modal logic
  var errorModal = document.getElementById("error-modal");
  if (errorModal && errorModal.getAttribute("data-show") === "true") {
    errorModal.classList.add('is-active');
  }
  var closeErrorBtn = document.getElementById("close-error-modal-btn");
  if (closeErrorBtn) {
    closeErrorBtn.onclick = function() {
      errorModal.classList.remove('is-active');
    };
  }
  window.addEventListener('click', function(event) {
    if (event.target === errorModal) {
      errorModal.classList.remove('is-active');
    }
  });
});

function showFileName(input) {
  const fileNameSpan = document.getElementById('file-name');
  const maxSizeMB = 5;
  if (input.files && input.files.length > 0) {
    const file = input.files[0];
    if (file.size > maxSizeMB * 1024 * 1024) {
      alert("❌ File too large. Maximum allowed size is " + maxSizeMB + "MB.");
      input.value = "";
      fileNameSpan.textContent = "";
      return;
    }
    fileNameSpan.textContent = "Selected file: " + file.name;
  } else {
    fileNameSpan.textContent = "";
  }
}