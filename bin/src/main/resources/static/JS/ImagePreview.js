document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('nrc_front').addEventListener('change', function(event) {
        const file = event.target.files[0];
        const preview = document.getElementById('preview_front');
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                preview.src = e.target.result;
                preview.classList.remove('hidden');
            };
            reader.readAsDataURL(file);
        }
    });

    document.getElementById('nrc_back').addEventListener('change', function(event) {
        const file = event.target.files[0];
        const preview = document.getElementById('preview_back');
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                preview.src = e.target.result;
                preview.classList.remove('hidden');
            };
            reader.readAsDataURL(file);
        }
    });
});
