package com.alperensarac.ebiletjetpack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/*
    CommonComponents.kt

    Projede tekrar tekrar kullanacağımız küçük UI parçalarını burada tutuyoruz.

    Neden?
    - Kod tekrarı azalır.
    - Tasarım tek yerden yönetilir.
    - Login/Register/Home ekranlarında aynı buton ve text field yapısını kullanabiliriz.
*/

/*
    Projede sık kullanacağımız renkler.
    İleride bunları theme içine taşıyabiliriz.
*/
val AppBlue = Color(0xFF2563EB)
val AppGreen = Color(0xFF16A34A)
val AppBackground = Color(0xFFF5F6FA)
val AppTextDark = Color(0xFF0F172A)
val AppTextGray = Color(0xFF64748B)

/*
    Normal input alanı.

    Compose tarafında XML'deki EditText yerine OutlinedTextField kullanıyoruz.

    Parametreler:
    value:
        TextField içinde yazan mevcut değer.

    onValueChange:
        Kullanıcı yazdıkça state'i güncelleyen fonksiyon.

    label:
        Alanın başlığı.

    isError:
        Hata varsa kırmızı görünür.

    visualTransformation:
        Şifre gizleme gibi görsel dönüşümler için.
*/
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label)
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = isError,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            errorContainerColor = Color.White,
            focusedIndicatorColor = AppBlue,
            focusedLabelColor = AppBlue
        )
    )
}

/*
    Ana buton.

    isLoading true ise:
    - Buton pasif olur
    - Yazı yerine küçük loading gösterilir
*/
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppBlue,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.White
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White
            )
        } else {
            Text(text = text)
        }
    }
}

/*
    Link gibi çalışan TextButton.

    Login/Register geçişlerinde kullanacağız.
*/
@Composable
fun AppLinkButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Text(
            text = text,
            color = AppBlue
        )
    }
}

/*
    Bu dosyada önceki Login/Register componentleri zaten vardı.

    Eğer dosyanın içinde importlar zaten varsa:
    - Aynı importları ikinci kez eklemene gerek yok.
    - Sadece aşağıdaki yeni composable fonksiyonları dosyanın en altına eklemen yeterli.
*/

/*
    AppTopBar

    Compose tarafında XML'deki üst bar mantığını bu component ile kuruyoruz.

    Kullanım:
    - HomeScreen
    - EventDetailScreen
    - MyTicketsScreen
    - TicketDetailScreen
*/
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    actions: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBlue)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    color = Color(0xFFDBEAFE)
                )
            }
        }

        if (actions != null) {
            Row {
                actions()
            }
        }
    }
}

/*
    Küçük beyaz buton.

    Üst barda:
    - Biletlerim
    - QR Kontrol
    - Çıkış

    gibi butonlarda kullanacağız.
*/
@Composable
fun AppSmallWhiteButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = AppBlue
        )
    ) {
        Text(text = text)
    }
}

/*
    Beyaz kart container.

    Home filtre alanı gibi yerlerde kullanacağız.
*/
@Composable
fun AppWhiteCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}