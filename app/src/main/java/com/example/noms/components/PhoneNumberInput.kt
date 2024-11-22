package com.example.noms.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val seaGreen = Color(0xFF2E8B57)

    var countryCode by remember { mutableStateOf("+1") }
    var expanded by remember { mutableStateOf(false) }

    val countryCodes = listOf("+1", "+44", "+91", "+61", "+81", "+86", "+49", "+33", "+39", "+7")

    var rawPhoneNumber by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = countryCode,
                onValueChange = { },
                readOnly = true,
                label = { Text("Code", color = seaGreen) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .width(100.dp)
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = seaGreen,
                    unfocusedBorderColor = seaGreen,
                ),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                countryCodes.forEach { code ->
                    DropdownMenuItem(
                        text = { Text(text = code) },
                        onClick = {
                            countryCode = code
                            expanded = false
                        },
						modifier = Modifier.background(Color.White)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = rawPhoneNumber,
            onValueChange = { input ->
                if (input.length <= 10 && input.all { it.isDigit() }) {
                    rawPhoneNumber = input
                    // Format: +1 XXX-XXX-XXXX
                    val formatted = "${countryCode}$input"
                    onValueChange(formatted)
                }
            },
            label = { Text("Phone Number", color = seaGreen) },
            modifier = Modifier
                .weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = seaGreen,
                unfocusedBorderColor = seaGreen,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
    }
}