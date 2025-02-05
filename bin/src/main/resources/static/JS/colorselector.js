// Map of colors to corresponding image URLs
const colorImageMap = {
    pink: 'pink-blouse.jpg',
    blue: 'blue-blouse.jpg',
    black: 'black-blouse.jpg'
  };
  
  // Add event listeners to all color buttons
  const colorButtons = document.querySelectorAll('.color-button');
  const productImage = document.getElementById('product-image');
  
  colorButtons.forEach(button => {
    button.addEventListener('click', () => {
      const selectedColor = button.getAttribute('data-color');
      productImage.src = colorImageMap[selectedColor];
      productImage.alt = `${selectedColor.charAt(0).toUpperCase() + selectedColor.slice(1)} Blouse`;
    });
  });
  