package com.example.lab5;




import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editTextName;
    EditText editTextPrice;
    Button buttonAddProduct;
    ListView listViewProducts;

    List<Product> products;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("products");

        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextPrice = (EditText) findViewById(R.id.editTextPrice);
        listViewProducts = (ListView) findViewById(R.id.listViewProducts);
        buttonAddProduct = (Button) findViewById(R.id.addButton);

        products = new ArrayList<>();

        //adding an onclicklistener to button
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });

        listViewProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, "Item clicked", Toast.LENGTH_SHORT).show();  // ✅ Debug line
                Product product = products.get(i);
                showUpdateDeleteDialog(product.getId(), product.getProductName());
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();

        dbRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                products.clear();  // Clear the list to prevent duplicates

                for (com.google.firebase.database.DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Product product = postSnapshot.getValue(Product.class);
                    products.add(product);
                }

                ProductList adapter = new ProductList(MainActivity.this, products);
                listViewProducts.setAdapter(adapter);
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showUpdateDeleteDialog(final String productId, String productName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
        final EditText editTextPrice  = (EditText) dialogView.findViewById(R.id.editTextPrice);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdateProduct);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDeleteProduct);

        Product currentProduct = findProductById(productId);
        if (currentProduct != null) {
            editTextName.setText(productName);
            editTextPrice.setText(String.valueOf(currentProduct.getPrice()));
        }




        dialogBuilder.setTitle(productName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                double price = Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));
                if (!TextUtils.isEmpty(name)) {
                    updateProduct(productId, name, price);
                    b.dismiss();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct(productId);
                b.dismiss();
            }
        });
    }

    private void updateProduct(String id, String name, double price) {
        Product product = new Product(id, name, price);
        dbRef.child(id).setValue(product);
        Toast.makeText(getApplicationContext(), "Product updated", Toast.LENGTH_LONG).show();
    }

    private void deleteProduct(String id) {
        dbRef.child(id).removeValue();
        Toast.makeText(getApplicationContext(), "Product deleted", Toast.LENGTH_LONG).show();
    }

    private void addProduct() {
        String name = editTextName.getText().toString().trim();
        String priceText = editTextPrice.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(priceText)) {
            double price = Double.parseDouble(priceText);
            String id = dbRef.push().getKey(); // unique product ID
            Product product = new Product(id, name, price);
            dbRef.child(id).setValue(product);

            Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show();
            editTextName.setText("");
            editTextPrice.setText("");
        } else {
            Toast.makeText(this, "Please enter a name and price", Toast.LENGTH_SHORT).show();
        }
    }

    private Product findProductById(String id) {
        for (Product p : products) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }
}