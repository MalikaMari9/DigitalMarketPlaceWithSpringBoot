document.addEventListener("DOMContentLoaded", () => {
    console.log("🚀 JavaScript Loaded!");

    // ✅ Elements for Delivery Fee & Total Price Update
    const deliveryOption = document.getElementById("deliveryOption");
    const deliveryAddress = document.getElementById("deliveryAddress");
    const serviceFeeText = document.getElementById("serviceFeeText");
    const locationFeeText = document.getElementById("locationFeeText");
    const deliveryFeeText = document.getElementById("deliveryFeeText");
    const totalPriceText = document.getElementById("totalPriceText");
    const subtotalText = document.getElementById("subtotalText");
	
	//For Payment
	const paymentRadios = document.querySelectorAll('input[name="paymentMethod"]');
	   const creditCardSection = document.querySelector(".credit-card-selection");
	   const selectedCardDropdown = document.getElementById("selectedCard");
	

    function updateDeliveryFee() {
        let subtotal = parseFloat(subtotalText.innerText.replace('$', '').trim()) || 0;
        let serviceFee = deliveryOption?.value === "express" ? 10.00 : 5.00;

        // ✅ Get Selected Address for Buyer
        let selectedAddress = deliveryAddress.options[deliveryAddress.selectedIndex];
        if (!selectedAddress.value) {
            console.warn("⚠️ No address selected.");
            serviceFeeText.innerText = `$${serviceFee.toFixed(2)}`;
            locationFeeText.innerText = `$--`;
            deliveryFeeText.innerText = `$--`;
            totalPriceText.innerText = `$--`;
            return;
        }

        // ✅ Get Buyer's City & Town from the selected option
        let buyerCity = selectedAddress.getAttribute("data-city");
        let buyerTown = selectedAddress.getAttribute("data-town");
        let sellerCity = deliveryAddress.dataset.sellerCity;
        let sellerTown = deliveryAddress.dataset.sellerTown;

        console.log("📌 Seller City:", sellerCity);
        console.log("📌 Seller Town:", sellerTown);
        console.log("🛒 Buyer Selected Address:");
        console.log("   📍 City:", buyerCity);
        console.log("   📍 Town:", buyerTown);

        // ✅ Calculate Location-Based Fee
        let locationFee = 10.00; // Default: Different City
        if (buyerTown === sellerTown) {
            locationFee = 2.00; // ✅ Same Town
        } else if (buyerCity === sellerCity) {
            locationFee = 5.00; // ✅ Same City, Different Town
        }

        console.log("🚚 Service Fee:", serviceFee);
        console.log("📍 Location-Based Fee:", locationFee);

        // ✅ Calculate Total Delivery Fee (Service Fee + Location Fee)
        let totalDeliveryFee = serviceFee + locationFee;

        // ✅ Update UI with fees
        serviceFeeText.innerText = `$${serviceFee.toFixed(2)}`;
        locationFeeText.innerText = `$${locationFee.toFixed(2)}`;
        deliveryFeeText.innerText = `$${totalDeliveryFee.toFixed(2)}`;
        totalPriceText.innerText = `$${(subtotal + totalDeliveryFee).toFixed(2)}`;
    }

    // ✅ Event Listeners for Fee Update
    if (deliveryOption) deliveryOption.addEventListener("change", updateDeliveryFee);
    if (deliveryAddress) deliveryAddress.addEventListener("change", updateDeliveryFee);
    window.addEventListener("load", updateDeliveryFee);

	// ✅ Toggle Credit Card Selection Visibility
	    paymentRadios.forEach((radio) => {
	        radio.addEventListener("change", () => {
	            creditCardSection.style.display = radio.value === "online" ? "block" : "none";
	        });
	    });
	
    // ✅ Order Placement Logic
    document.querySelectorAll(".btn-place-order").forEach((btn) => {
        btn.addEventListener("click", async (event) => {
            event.preventDefault();
            console.log("✅ Place Order button clicked!");

            // ✅ Fetch input values
            const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked')?.value;
            const deliveryOptionValue = deliveryOption?.value;
            const addressID = deliveryAddress?.value;
            const sellerID = btn.getAttribute("data-seller-id");

            // ✅ Ensure values are correctly fetched
            if (!sellerID) {
                alert("❌ Missing seller ID!");
                return;
            }
            if (!paymentMethod) {
                alert("❌ Please select a payment method.");
                return;
            }
            if (!addressID) {
                alert("❌ Please select a delivery address.");
                return;
            }

            // ✅ Fetch the calculated delivery fee dynamically
            let totalDeliveryFee = parseFloat(deliveryFeeText.innerText.replace('$', '').trim()) || 0;

            // ✅ Fetch the subtotal dynamically
            let subtotal = parseFloat(subtotalText.innerText.replace('$', '').trim()) || 0;
            let totalAmount = (subtotal + totalDeliveryFee).toFixed(2);

            // ✅ Debugging Log
            console.log("🚀 DEBUGGING ORDER DATA:");
            console.log(`🛒 Subtotal: $${subtotal}`);
            console.log(`💰 Total Delivery Fee: $${totalDeliveryFee}`);
            console.log(`💵 Final Total Amount: $${totalAmount}`);
            console.log(`👤 Seller ID: ${sellerID}`);
            console.log(`🏠 Address ID: ${addressID}`);
            console.log(`💳 Payment Method: ${paymentMethod}`);
            console.log(`🚚 Delivery Option: ${deliveryOptionValue}`);
            console.log("✅ Data ready for backend request!");

            // ✅ Redirect for online payment
			if (paymentMethod === "online") {
			               const selectedCardID = selectedCardDropdown?.value;

			               if (!selectedCardID) {
			                   alert("❌ Please select a credit card.");
			                   return;
			               }

			               try {
			                   const response = await fetch("/place-order", {
			                       method: "POST",
			                       headers: { "Content-Type": "application/json" },
			                       body: JSON.stringify({
			                           paymentMethod,
			                           deliveryOption: deliveryOptionValue,
			                           addressID: parseInt(addressID),
			                           deliveryFee: totalDeliveryFee,
			                           sellerID: parseInt(sellerID),
			                           totalAmount,
			                           selectedCardID // ✅ Include selected credit card ID
			                       })
			                   });

			                   const data = await response.json();
			                   console.log("🔍 Response:", data);

			                   if (response.ok && data.success) {
			                       alert("✅ Order placed successfully!");
			                       window.location.href = "/orderHistory"; // Redirect to confirmation page
			                   } else {
			                       alert("❌ Failed to place order: " + (data.message || "Unknown error"));
			                   }
			               } catch (error) {
			                   console.error("❌ Fetch error:", error);
			                   alert("❌ An error occurred while placing the order.");
			               }
			               return;
			           }

            // ✅ Otherwise, process the order (COD)
            try {
                const response = await fetch("/place-order", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        paymentMethod: paymentMethod,
                        deliveryOption: deliveryOptionValue,
                        addressID: parseInt(addressID),
                        deliveryFee: totalDeliveryFee,  // ✅ Send final delivery fee from frontend
                        sellerID: parseInt(sellerID),
                        totalAmount: totalAmount
                    })
                });

                const data = await response.json();
                console.log("🔍 Response:", data);

                if (response.ok && data.success) {
                    alert("✅ Order placed successfully!");
                    window.location.href = "/orderHistory"; // Redirect to confirmation page
                } else {
                    alert("❌ Failed to place order: " + (data.message || "Unknown error"));
                }
            } catch (error) {
                console.error("❌ Fetch error:", error);
                alert("❌ An error occurred while placing the order.");
            }
        });
    });
});
