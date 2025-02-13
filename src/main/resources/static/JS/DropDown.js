document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("citycode").addEventListener("change", function () {
        var cityCode = this.value;
        var nameDropdown = document.getElementById("name_en");

        // Clear previous options
        nameDropdown.innerHTML = '<option value="">-- Select Area Name --</option>';

        if (cityCode !== "0") {
            fetch(`/getNames?cityCode=${cityCode}`)
                .then(response => response.json())
                .then(data => {
                    data.forEach(name => {
                        var option = document.createElement("option");
                        option.value = name;
                        option.textContent = name;
                        nameDropdown.appendChild(option);
                    });
                })
                .catch(error => console.error("Error fetching names:", error));
        }
    });
});
