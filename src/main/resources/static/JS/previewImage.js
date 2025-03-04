document.addEventListener('DOMContentLoaded', function () {
    const imageInput = document.getElementById('item-images');
    const previewContainer = document.getElementById('image-preview-container');
    const dt = new DataTransfer(); // To modify the FileList
    const maxFiles = 5; // Maximum number of files
    const maxWidth = 1920; // Max image width
    const maxHeight = 1080; // Max image height
    const maxSize = parseInt(imageInput.getAttribute("data-max-size"), 10) || (5 * 1024 * 1024); // Get from HTML

    imageInput.addEventListener('change', function (event) {
        Array.from(event.target.files).forEach(file => {
            let isDuplicate = false;
            for (let i = 0; i < dt.items.length; i++) {
                if (dt.items[i].getAsFile().name === file.name && dt.items[i].getAsFile().size === file.size) {
                    isDuplicate = true;
                    break;
                }
            }

            if (dt.items.length >= maxFiles) {
                alert(`You can only upload a maximum of ${maxFiles} images.`);
                return;
            }

            if (file.size > maxSize) { // ✅ Now dynamically checks the app property limit
                alert(`Image ${file.name} is too large. Max size is ${maxSize / (1024 * 1024)}MB.`);
                return;
            }

            const img = new Image();
            img.src = URL.createObjectURL(file);
            img.onload = function () {
                if (img.width > maxWidth || img.height > maxHeight) {
                    alert(`Image ${file.name} exceeds max resolution (${maxWidth}x${maxHeight}).`);
                    return;
                }

                if (!isDuplicate) {
                    dt.items.add(file);

                    const previewDiv = document.createElement('div');
                    previewDiv.style.display = 'inline-block';
                    previewDiv.style.margin = '10px';
                    previewDiv.style.position = 'relative';

                    const imgElement = document.createElement('img');
                    imgElement.src = img.src;
                    imgElement.style.maxWidth = '150px';
                    imgElement.style.border = '1px solid #ccc';
                    imgElement.style.borderRadius = '5px';

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
                        for (let i = 0; i < dt.items.length; i++) {
                            if (dt.items[i].getAsFile() === file) {
                                dt.items.remove(i);
                                break;
                            }
                        }

                        imageInput.files = dt.files;
                        previewDiv.remove();
                    });

                    previewDiv.appendChild(imgElement);
                    previewDiv.appendChild(removeBtn);
                    previewContainer.appendChild(previewDiv);
                }

                imageInput.files = dt.files;
            };
        });
    });
});
