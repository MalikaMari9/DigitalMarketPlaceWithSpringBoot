function fetchNames() {
          var citycode = document.getElementById("citycode").value;
          var xhr = new XMLHttpRequest();
          xhr.open("GET", "FetchNamesServlet?citycode=" + citycode, true);
          xhr.onreadystatechange = function () {
              if (xhr.readyState === 4 && xhr.status === 200) {
                  var names = JSON.parse(xhr.responseText);
                  var nameDropdown = document.getElementById("name_en");
                  nameDropdown.innerHTML = ""; // Clear existing options
                  names.forEach(function (name) {
                      var option = document.createElement("option");
                      option.value = name;
                      option.text = name;
                      nameDropdown.add(option);
                  });
              }
          };
          xhr.send();
      }