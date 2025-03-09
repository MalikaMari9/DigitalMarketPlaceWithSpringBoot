package com.example.demo.controller;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Address;
import com.example.demo.entity.Order;
import com.example.demo.entity.Receipt;
import com.example.demo.repository.ReceiptRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;

@RestController
@RequestMapping("/download-invoice")
public class InvoiceController {

	@Autowired
	private ReceiptRepository receiptRepository;

	@GetMapping("/{receiptID}")
	public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long receiptID) {
		Receipt receipt = receiptRepository.findById(receiptID).orElse(null);
		if (receipt == null) {
			return ResponseEntity.notFound().build();
		}

		// Generate PDF
		byte[] pdfBytes = generateInvoice(receiptID, receipt);
		if (pdfBytes == null) {
			return ResponseEntity.internalServerError().build();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("attachment", "Invoice_" + receiptID + ".pdf");

		return ResponseEntity.ok().headers(headers).body(pdfBytes);
	}

	private byte[] generateInvoice(Long receiptID, Receipt receipt) {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			PdfWriter writer = new PdfWriter(outputStream);
			PdfDocument pdfDoc = new PdfDocument(writer);
			Document document = new Document(pdfDoc);

			// Title
			document.add(new Paragraph("Invoice - Receipt ID: " + receiptID).setBold().setFontSize(16));

			// Buyer Info
			document.add(new Paragraph("Buyer: " + receipt.getBuyer().getUsername()));

			// Buyer Address
			Optional<Address> buyerAddress = receipt.getBuyer().getAddresses().stream().findFirst();
			double locationFee = 0.0;
			if (buyerAddress.isPresent()) {
				Address buyer = buyerAddress.get();
				document.add(new Paragraph(
						"Address: " + buyer.getAddres() + ", " + buyer.getCity() + ", " + buyer.getTownship()));
				document.add(new Paragraph("Phone: " + buyer.getPhone()));

				// Fetch the seller's first available address
				Optional<Address> sellerAddress = receipt.getSeller().getAddresses().stream().findFirst();
				if (sellerAddress.isPresent()) {
					Address sellerAddr = sellerAddress.get();
					if (buyer.getCity().equalsIgnoreCase(sellerAddr.getCity())) {
						if (buyer.getTownship().equalsIgnoreCase(sellerAddr.getTownship())) {
							locationFee = 2.0; // Same town
						} else {
							locationFee = 5.0; // Same city, different town
						}
					} else {
						locationFee = 10.0; // Different city
					}
				}
			} else {
				document.add(new Paragraph("Address: Not provided"));
			}

			// Seller Info (WITHOUT Address)
			document.add(new Paragraph("Seller: " + receipt.getSeller().getUsername()));

			double totalDeliveryFee = receipt.getDeliFee() + locationFee;

			// Delivery Fees Breakdown
			document.add(new Paragraph("Delivery Service Fee: USD " + receipt.getDeliFee()));
			document.add(new Paragraph("Location-Based Fee: USD " + locationFee));
			document.add(new Paragraph("Total Delivery Fee: USD " + totalDeliveryFee));

			// Table for Order Details
			Table table = new Table(4);
			table.addCell(new Cell().add(new Paragraph("Item Name").setBold()));
			table.addCell(new Cell().add(new Paragraph("Price").setBold()));
			table.addCell(new Cell().add(new Paragraph("Quantity").setBold()));
			table.addCell(new Cell().add(new Paragraph("Total").setBold()));

			for (Order order : receipt.getOrders()) {
				table.addCell(new Cell().add(new Paragraph(order.getItem().getItemName())));
				table.addCell(new Cell().add(new Paragraph("USD " + order.getPrice())));
				table.addCell(new Cell().add(new Paragraph(String.valueOf(order.getQuantity()))));
				table.addCell(new Cell().add(new Paragraph("USD " + (order.getPrice() * order.getQuantity()))));
			}

			// Total Price Row
			table.addCell(new Cell(1, 3).add(new Paragraph("Total Price:").setBold())
					.setTextAlignment(TextAlignment.RIGHT).setBorder(null));

			table.addCell(new Cell().add(new Paragraph("USD " + receipt.getTotalPrice()))
					.setTextAlignment(TextAlignment.RIGHT).setBorder(null));

			document.add(table);
			document.close();

			return outputStream.toByteArray(); // Convert PDF to byte array
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}