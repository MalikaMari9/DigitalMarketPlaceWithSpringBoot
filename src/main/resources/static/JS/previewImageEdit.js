document.addEventListener('DOMContentLoaded', function () {
    const imageInput = document.getElementById('item-images');
    const previewContainer = document.getElementById('image-preview');
    const dt = new DataTransfer(); // To modify the FileList

    const maxFiles = 5; // Maximum number of files
    const maxWidth = 1920; // Max image width
    const maxHeight = 1080; // Max image height
    const maxSize = 5 * 1024 * 1024; // 5MB max file size

    // ✅ Count existing images in previewContainer
    let existingImagesCount = previewContainer.children.length;

    // ✅ Remove "required" attribute if images already exist
    if (existingImagesCount > 0) {
        imageInput.removeAttribute("required");
    }

    imageInput.addEventListener('change', function (event) {
        let newFiles = Array.from(event.target.files);

        // ✅ Calculate total images (existing + new)
        let totalImages = existingImagesCount + dt.files.length + newFiles.length;

        // ✅ Prevent exceeding max file count
        if (totalImages > maxFiles) {
            alert(`⚠️ You can only upload up to ${maxFiles} images. You already have ${existingImagesCount} uploaded.`);
            return;
        }

        newFiles.forEach(file => {
            // Check for duplicate images
            let isDuplicate = Array.from(dt.files).some(existingFile =>
                existingFile.name === file.name && existingFile.lastModified === file.lastModified
            );
            if (isDuplicate) {
                alert(`⚠️ Image ${file.name} is already uploaded.`);
                return;
            }

            // Check file size
            if (file.size > maxSize) {
                alert(`⚠️ Image ${file.name} is too large. Max size is 5MB.`);
                return;
            }

            // Check image resolution
            const img = new Image();
            img.src = URL.createObjectURL(file);
            img.onload = function () {
                if (img.width > maxWidth || img.height > maxHeight) {
                    alert(`⚠️ Image ${file.name} exceeds max resolution (${maxWidth}x${maxHeight}).`);
                    return;
                }

                if (!isDuplicate) {
                    // Add file to DataTransfer
                    dt.items.add(file);

                    // Create wrapper div
                    const wrapper = document.createElement('div');
                    wrapper.className = "image-wrapper"; // CSS Class for styling

                    // Create image preview
                    const imgElement = document.createElement('img');
                    imgElement.src = img.src;
                    imgElement.className = "image-preview-thumbnail";

                    // Create remove button
                    const removeBtn = document.createElement('button');
                    removeBtn.className = "remove-image-btn";
                    removeBtn.innerHTML = "✖";
                    removeBtn.onclick = function () {
                        removeUploadedImage(file, wrapper);
                    };

                    // Append elements to wrapper
                    wrapper.appendChild(imgElement);
                    wrapper.appendChild(removeBtn);
                    previewContainer.appendChild(wrapper);
                    
                    // ✅ Update existingImagesCount when new images are added
                    existingImagesCount++;
                }

                // ✅ Update file input FileList
                imageInput.files = dt.files;
            };
        });
    });



    /** ✅ Remove Uploaded Image */
    function removeUploadedImage(file, wrapper) {
        for (let i = 0; i < dt.items.length; i++) {
            if (dt.items[i].getAsFile().name === file.name && dt.items[i].getAsFile().lastModified === file.lastModified) {
                dt.items.remove(i);
                break;
            }
        }
        imageInput.files = dt.files; // Update input
        wrapper.remove(); // Remove preview

        // ✅ If no images left, re-add "required" attribute
        if (previewContainer.children.length === 0 && dt.items.length === 0) {
            imageInput.setAttribute("required", "required");
        }
    }
});


/** ✅ Remove Existing Image from Backend */
function removeImage(button) {
    const imageWrapper = button.parentElement;
    const fullPath = button.getAttribute("data-image"); // Full path (e.g., /Image/Item/5/images.jpg)
    const itemId = button.getAttribute("data-item-id"); // Item ID

    // Extract only the file name from the full path
    const fileName = fullPath.substring(fullPath.lastIndexOf("/") + 1);

    fetch('/delete-item-image', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ imagePath: fileName, itemId: itemId }) // Send only fileName and itemId
    })
    .then(response => {
        if (response.ok) {
            imageWrapper.remove(); // ✅ Remove from UI
        } else {
            response.text().then(errorMsg => {
                alert("❌ Failed to delete image: " + errorMsg);
            });
        }
    })
    .catch(error => console.error("Error deleting image:", error));
}

