package co.edu.udea.compumovil.gr04_20261.lab1

import android.content.res.Configuration
import android.os.Bundle
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 1. Modelo de datos para la API
data class CityResponse(val name: String)

// 2. Interfaz de Retrofit
interface ColombiaApi {
    @GET("api/v1/City")
    suspend fun getCities(): List<CityResponse>
}

class ContactDataActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuración de Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-colombia.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ColombiaApi::class.java)

        setContent {
            // Estado para la lista de ciudades de la API
            var cityList by remember { mutableStateOf(listOf<String>()) }
            
            // Cargar ciudades al iniciar
            LaunchedEffect(Unit) {
                try {
                    val response = api.getCities()
                    cityList = response.map { it.name }.sorted()
                } catch (e: Exception) {
                    // Lista de respaldo en caso de error de red
                    cityList = listOf("Bogotá", "Medellín", "Cali", "Barranquilla", "Cartagena")
                }
            }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContactDataScreen(cityList)
                }
            }
        }
    }
}

@Composable
fun ContactDataScreen(availableCities: List<String>) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var telefono by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var pais by rememberSaveable { mutableStateOf("") }
    var ciudad by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Información de Contacto",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isLandscape) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ContactField(
                    value = telefono,
                    onValueChange = { telefono = it.filter { c -> c.isDigit() } },
                    label = "*Teléfono",
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                ContactField(
                    value = email,
                    onValueChange = { email = it },
                    label = "*Email",
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ContactField(
                    value = pais,
                    onValueChange = { pais = it },
                    label = "*País",
                    icon = Icons.Default.Public,
                    modifier = Modifier.weight(1f)
                )
                CityAutocompleteField(
                    value = ciudad,
                    onValueChange = { ciudad = it },
                    cities = availableCities,
                    modifier = Modifier.weight(1f)
                )
            }
            ContactField(
                value = direccion,
                onValueChange = { direccion = it },
                label = "Dirección",
                icon = Icons.Default.Home,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            ContactField(value = telefono, onValueChange = { telefono = it.filter { c -> c.isDigit() } }, label = "*Teléfono", icon = Icons.Default.Phone, keyboardType = KeyboardType.Number)
            ContactField(value = email, onValueChange = { email = it }, label = "*Email", icon = Icons.Default.Email, keyboardType = KeyboardType.Email)
            ContactField(value = pais, onValueChange = { pais = it }, label = "*País", icon = Icons.Default.Public)
            CityAutocompleteField(value = ciudad, onValueChange = { ciudad = it }, cities = availableCities)
            ContactField(value = direccion, onValueChange = { direccion = it }, label = "Dirección", icon = Icons.Default.Home)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                println("Teléfono: $telefono, Dirección: $direccion, Email: $email, País: $pais, Ciudad: $ciudad")
            },
            modifier = Modifier.align(if (isLandscape) Alignment.End else Alignment.CenterHorizontally)
        ) {
            Text("Siguiente")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    cities: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredCities = if (value.isNotEmpty()) {
        cities.filter { it.contains(value, ignoreCase = true) }.take(5)
    } else {
        emptyList()
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationCity,
            contentDescription = null,
            modifier = Modifier.size(32.dp).padding(end = 8.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        ExposedDropdownMenuBox(
            expanded = expanded && filteredCities.isNotEmpty(),
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    expanded = true
                },
                label = { Text("Ciudad") },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            
            ExposedDropdownMenu(
                expanded = expanded && filteredCities.isNotEmpty(),
                onDismissRequest = { expanded = false }
            ) {
                filteredCities.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onValueChange(selectionOption)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
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
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp).padding(end = 8.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next)
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun ContactDataScreenLandscapePreview() {
    ContactDataScreen(listOf("Bogotá", "Medellín", "Cali"))
}
