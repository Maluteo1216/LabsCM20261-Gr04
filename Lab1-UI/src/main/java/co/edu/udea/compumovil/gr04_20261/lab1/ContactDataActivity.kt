package co.edu.udea.compumovil.gr04_20261.lab1

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// ---------- API de ciudades ----------

data class CityResponse(val name: String)

interface ColombiaApi {
    @GET("api/v1/City")
    suspend fun getCities(): List<CityResponse>
}

// Países de Latinoamérica — lista estática
val latinAmericanCountries = listOf(
    "Argentina", "Bolivia", "Brasil", "Chile", "Colombia", "Costa Rica",
    "Cuba", "Ecuador", "El Salvador", "Guatemala", "Haití", "Honduras",
    "México", "Nicaragua", "Panamá", "Paraguay", "Perú", "Puerto Rico",
    "República Dominicana", "Uruguay", "Venezuela"
)

// ---------- Activity ----------

class ContactDataActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recuperar datos personales enviados desde PersonalDataActivity
        val personalNames     = intent.getStringExtra(PersonalDataKeys.NAMES)      ?: ""
        val personalLastNames = intent.getStringExtra(PersonalDataKeys.LAST_NAMES) ?: ""
        val personalGender    = intent.getStringExtra(PersonalDataKeys.GENDER)     ?: ""
        val personalBirthDate = intent.getStringExtra(PersonalDataKeys.BIRTH_DATE) ?: ""
        val personalEducation = intent.getStringExtra(PersonalDataKeys.EDUCATION)  ?: ""

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-colombia.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ColombiaApi::class.java)

        setContent {
            var cityList by rememberSaveable { mutableStateOf(listOf<String>()) }

            LaunchedEffect(Unit) {
                if (cityList.isEmpty()) {
                    try {
                        cityList = api.getCities().map { it.name }.sorted()
                    } catch (e: Exception) {
                        cityList = listOf(
                            "Barranquilla", "Bogotá", "Bucaramanga", "Cali",
                            "Cartagena", "Cúcuta", "Ibagué", "Manizales",
                            "Medellín", "Montería", "Neiva", "Pasto",
                            "Pereira", "Santa Marta", "Villavicencio"
                        )
                    }
                }
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ContactDataScreen(
                        availableCities  = cityList,
                        personalNames    = personalNames,
                        personalLastNames = personalLastNames,
                        personalGender   = personalGender,
                        personalBirthDate = personalBirthDate,
                        personalEducation = personalEducation
                    )
                }
            }
        }
    }
}

// ---------- Pantalla ----------

@Composable
fun ContactDataScreen(
    availableCities: List<String>,
    personalNames: String,
    personalLastNames: String,
    personalGender: String,
    personalBirthDate: String,
    personalEducation: String
) {
    val configuration = LocalConfiguration.current
    val isLandscape   = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context       = LocalContext.current

    var telefono  by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var email     by rememberSaveable { mutableStateOf("") }
    var pais      by rememberSaveable { mutableStateOf("") }
    var ciudad    by rememberSaveable { mutableStateOf("") }

    // Strings para labels y validación (multilenguaje)
    val labelPhone   = stringResource(R.string.label_phone)
    val labelEmail   = stringResource(R.string.label_email)
    val labelCountry = stringResource(R.string.label_country)
    val labelCity    = stringResource(R.string.label_city)
    val labelAddress = stringResource(R.string.label_address)
    val errorMsg     = stringResource(R.string.error_required_fields)
    val btnSubmit    = stringResource(R.string.btn_submit)

    // Strings de género para mostrar en el log en el idioma correcto
    val genderMaleStr   = stringResource(R.string.gender_male)
    val genderFemaleStr = stringResource(R.string.gender_female)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(id = R.string.title_contact_info),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isLandscape) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ContactField(
                    value = telefono,
                    onValueChange = { telefono = it.filter { c -> c.isDigit() } },
                    label = labelPhone, icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone, modifier = Modifier.weight(1f)
                )
                ContactField(
                    value = email, onValueChange = { email = it },
                    label = labelEmail, icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email, modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AutocompleteField(
                    value = pais, onValueChange = { pais = it },
                    label = labelCountry, icon = Icons.Default.Public,
                    suggestions = latinAmericanCountries, modifier = Modifier.weight(1f)
                )
                AutocompleteField(
                    value = ciudad, onValueChange = { ciudad = it },
                    label = labelCity, icon = Icons.Default.LocationCity,
                    suggestions = availableCities, modifier = Modifier.weight(1f)
                )
            }
            ContactField(
                value = direccion, onValueChange = { direccion = it },
                label = labelAddress, icon = Icons.Default.Home,
                keyboardType = KeyboardType.Text, autoCorrect = false,
                imeAction = ImeAction.Done,        // último campo → "Listo" en teclado
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            ContactField(
                value = telefono,
                onValueChange = { telefono = it.filter { c -> c.isDigit() } },
                label = labelPhone, icon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone
            )
            ContactField(
                value = email, onValueChange = { email = it },
                label = labelEmail, icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )
            AutocompleteField(
                value = pais, onValueChange = { pais = it },
                label = labelCountry, icon = Icons.Default.Public,
                suggestions = latinAmericanCountries
            )
            AutocompleteField(
                value = ciudad, onValueChange = { ciudad = it },
                label = labelCity, icon = Icons.Default.LocationCity,
                suggestions = availableCities
            )
            // Dirección: sin sugerencias, último campo → ImeAction.Done ("Listo")
            ContactField(
                value = direccion, onValueChange = { direccion = it },
                label = labelAddress, icon = Icons.Default.Home,
                keyboardType = KeyboardType.Text, autoCorrect = false,
                imeAction = ImeAction.Done
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                // Validar campos obligatorios de contacto: teléfono, email, país
                if (telefono.isBlank() || email.isBlank() || pais.isBlank()) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                } else {
                    // Construir display de género en el idioma activo
                    val genderDisplay = when (personalGender) {
                        "Male"   -> genderMaleStr
                        "Female" -> genderFemaleStr
                        else     -> ""
                    }

                    // ── LOG según formato del enunciado ──────────────────────────────────
                    // Información personal: Pepito Perez Masculino Nació el 20/05/1999 Secundaria
                    val personalLog = buildString {
                        append("Información personal:\n")
                        append("$personalNames $personalLastNames\n")
                        if (genderDisplay.isNotBlank()) append("$genderDisplay\n")
                        append("Nació el $personalBirthDate\n")
                        if (personalEducation.isNotBlank()) append(personalEducation)
                    }

                    // Información de contacto:
                    // Teléfono: 455555
                    // Dirección: Cll 155 #45-123  (opcional)
                    // Email: pepito@correo.com
                    // País: Ecuador
                    // Ciudad: Quito  (opcional)
                    val contactLog = buildString {
                        append("Información de contacto:\n")
                        append("Teléfono: $telefono\n")
                        if (direccion.isNotBlank()) append("Dirección: $direccion\n")
                        append("Email: $email\n")
                        append("País: $pais")
                        if (ciudad.isNotBlank()) append("\nCiudad: $ciudad")
                    }

                    Log.d("AppData", personalLog)
                    Log.d("AppData", contactLog)
                }
            },
            modifier = Modifier.align(
                if (isLandscape) Alignment.End else Alignment.CenterHorizontally
            )
        ) {
            // "Enviar/Submit" — este es el botón final de la app
            Text(btnSubmit)
        }
    }
}

// ---------- Componentes reutilizables ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    suggestions: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = if (value.isNotEmpty()) {
        suggestions.filter { it.contains(value, ignoreCase = true) }.take(6)
    } else emptyList()

    ExposedDropdownMenuBox(
        expanded = expanded && filtered.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it); expanded = true },
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && filtered.isNotEmpty()) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(expanded = expanded && filtered.isNotEmpty(), onDismissRequest = { expanded = false }) {
            filtered.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onValueChange(option); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun ContactField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    autoCorrect: Boolean = true,
    imeAction: ImeAction = ImeAction.Next  // "Siguiente" por defecto; último campo usa Done
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            autoCorrectEnabled = autoCorrect,
            imeAction = imeAction
        )
    )
}