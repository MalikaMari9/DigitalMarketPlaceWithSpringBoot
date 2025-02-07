document.addEventListener('DOMContentLoaded', function () {
    const imageInput = document.getElementById('item-images');
    const previewContainer = document.getElementById('image-preview-container');
    const dt = new DataTransfer(); // To modify the FileList
    const maxFiles = 5; // Maximum number of files
    const maxWidth = 1920; // Max image width
    const maxHeight = 1080; // Max image height
    const maxSize = 5 * 1024 * 1024; // 5MB max file size

    imageInput.addEventListener('change', function (event) {
        Array.from(event.target.files).forEach(file => {
            // Check if file already exists
            let isDuplicate = false;
            for (let i = 0; i < dt.items.length; i++) {
                if (dt.items[i].getAsFile().name === file.name && dt.items[i].getAsFile().size === file.size) {
                    isDuplicate = true;
                    break;
                }
            }

            // Prevent adding more than maxFiles
            if (dt.items.length >= maxFiles) {
                alert(`You can only upload a maximum of ${maxFiles} images.`);
                return;
            }

            // Check file size
            if (file.size > maxSize) {
                alert(`Image ${file.name} is too large. Max size is 5MB.`);
                return;
            }

            // Check image resolution
            const img = new Image();
            img.src = URL.createObjectURL(file);
            img.onload = function () {
                if (img.width > maxWidth || img.height > maxHeight) {
                    alert(`Image ${file.name} exceeds max resolution (${maxWidth}x${maxHeight}).`);
                    return;
                }

                if (!isDuplicate) {
                    // Add file to DataTransfer
                    dt.items.add(file);

                    // Create preview div
                    const previewDiv = document.createElement('div');
                    previewDiv.style.display = 'inline-block';
                    previewDiv.style.margin = '10px';
                    previewDiv.style.position = 'relative';

                    // Create image preview
                    const imgElement = document.createElement('img');
                    imgElement.src = img.src;
                    imgElement.style.maxWidth = '150px';
                    imgElement.style.border = '1px solid #ccc';
                    imgElement.style.borderRadius = '5px';

                    // Create remove button
                    const removeBtn = document.createElement('button');
                    removeBtn.textContent = 'X';
                    removeBtn.style.position = 'absolute';
                    removeBtn.style.top = '5px';
                    removeBtn.style.right = '5px';
                    removeBtn.style.backgroundColor = '#ff4d4d';
                    removeBtn.style.color = '#fff';
                    removeBtn.style.border = 'none';
                    removeBtn.style.borderRadius = '50%';
                    removeBtn.style.width = '24px';
                    removeBtn.style.height = '24px';
                    removeBtn.style.cursor = 'pointer';

                    removeBtn.addEventListener('click', function (e) {
                        e.preventDefault();

                        // Remove file from DataTransfer
                        for (let i = 0; i < dt.items.length; i++) {
                            if (dt.items[i].getAsFile() === file) {
                                dt.items.remove(i);
                                break;
                            }
                        }

                        // Update FileList
                        imageInput.files = dt.files;

                        // Remove preview
                        previewDiv.remove();
                    });

                    // Append image & remove button to preview div
                    previewDiv.appendChild(imgElement);
                    previewDiv.appendChild(removeBtn);
                    previewContainer.appendChild(previewDiv);
                }
                
                // Update file input FileList
                imageInput.files = dt.files;
            };
        });
    });
});


