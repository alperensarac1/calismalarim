package com.example.qrkodolusturucujetpack.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qrkodolusturucujetpack.R

@Composable
fun BeyazButton(text:String){
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .border(
                BorderStroke(2.dp, color = colorResource(R.color.textColor)),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Text(
            text = text,
            style = TextStyle(color = colorResource(R.color.textColor), fontWeight = FontWeight.Bold)
        )
    }
}//:Function End

@Composable
fun SegmentedButton(condition:MutableState<Boolean>,text: String,text2:String){
    Button(
        onClick = {
            condition.value = true
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (condition.value) colorResource(R.color.btnSeciliBackground) else Color.Transparent
        )
    ) {
        Text(text = text, style = TextStyle(color = colorResource(R.color.textColor), fontSize = 20.sp, fontWeight = FontWeight.Bold))
    }
    Button(
        onClick = {
            condition.value = false
        },
        colors = ButtonDefaults.buttonColors(
            containerColor =  if (!condition.value) colorResource(R.color.btnSeciliBackground) else Color.Transparent
        )
    ) {
        Text("Ürünlerimiz", style = TextStyle(color = colorResource(R.color.textColor),fontSize = 20.sp, fontWeight = FontWeight.Bold))
    }
}//:Function End