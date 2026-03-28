package co.edu.udea.compumovil.gr04_20261.lab1

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import co.edu.udea.compumovil.gr04_20261.lab1.ui.theme.Labs20261Gr04Theme
import java.util.*

// Claves para pasar los datos personales a ContactDataActivity via Intent
object PersonalDataKeys {
    const val NAMES      = "personal_names"
    const val LAST_NAMES = "personal_last_names"
    const val GENDER     = "personal_gender"
    const val BIRTH_DATE = "personal_birth_date"
    const val EDUCATION  = "personal_education"
}

class PersonalDataActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Labs20261Gr04Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PersonalDataScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDataScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    var names         by rememberSaveable { mutableStateOf("") }
    var lastNames     by rememberSaveable { mutableStateOf("") }
    var gender        by rememberSaveable { mutableStateOf("") }
    var birthDate     by rememberSaveable { mutableStateOf("") }
    var educationLevel by rememberSaveable { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d -> birthDate = "$d/${m + 1}/$y" },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Leer strings fuera de lambdas (requerido por Compose)
    val genderMaleStr       = stringResource(R.string.gender_male)
    val genderFemaleStr     = stringResource(R.string.gender_female)
    val errorRequiredFields = stringResource(R.string.error_required_fields)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(id = R.string.title_personal_info),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isLandscape) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NameField(names, { names = it }, R.string.label_first_name, R.string.hint_names, Modifier.weight(1f))
                LastNameField(lastNames, { lastNames = it }, R.string.label_last_name, R.string.hint_apellidos, Modifier.weight(1f))
            }
        } else {
            NameField(names, { names = it }, R.string.label_first_name, R.string.hint_names)
            LastNameField(lastNames, { lastNames = it }, R.string.label_last_name, R.string.hint_apellidos)
        }

        // Sexo — campo opcional
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.selectableGroup().padding(vertical = 4.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.label_gender), fontWeight = FontWeight.Medium)
            GenderOption(selected = (gender == "Male"),   label = genderMaleStr,   onClick = { gender = "Male" })
            GenderOption(selected = (gender == "Female"), label = genderFemaleStr, onClick = { gender = "Female" })
        }

        // Fecha de nacimiento — obligatorio (*)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Cake, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.label_birth_date), fontWeight = FontWeight.Medium)
            Text(text = if (birthDate.isNotBlank()) birthDate else "—", modifier = Modifier.padding(start = 8.dp))
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { datePickerDialog.show() }) {
                Text(text = stringResource(id = R.string.btn_change))
            }
        }

        // Grado de escolaridad — opcional
        EducationDropdown(value = educationLevel, onValueChange = { educationLevel = it })

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (names.isBlank() || lastNames.isBlank() || birthDate.isBlank()) {
                    Toast.makeText(context, errorRequiredFields, Toast.LENGTH_LONG).show()
                } else {
                    // Pasar todos los datos personales al Intent para que ContactDataActivity
                    // pueda incluirlos en el log final junto con los datos de contacto.
                    val intent = Intent(context, ContactDataActivity::class.java).apply {
                        putExtra(PersonalDataKeys.NAMES,      names)
                        putExtra(PersonalDataKeys.LAST_NAMES, lastNames)
                        putExtra(PersonalDataKeys.GENDER,     gender)
                        putExtra(PersonalDataKeys.BIRTH_DATE, birthDate)
                        putExtra(PersonalDataKeys.EDUCATION,  educationLevel)
                    }
                    context.startActivity(intent)
                }
            },
            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
        ) {
            // "Siguiente" es correcto aquí: navega a la siguiente pantalla, aún no envía todo
            Text(text = stringResource(id = R.string.btn_next))
        }
    }
}

@Composable
fun NameField(value: String, onValueChange: (String) -> Unit, labelRes: Int, hintRes: Int, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(stringResource(id = labelRes)) },
        placeholder = { Text(stringResource(id = hintRes)) },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
fun LastNameField(value: String, onValueChange: (String) -> Unit, labelRes: Int, hintRes: Int, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(stringResource(id = labelRes)) },
        placeholder = { Text(stringResource(id = hintRes)) },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
fun GenderOption(selected: Boolean, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationDropdown(value: String, onValueChange: (String) -> Unit) {
    val options = listOf(
        stringResource(R.string.education_primary),
        stringResource(R.string.education_secondary),
        stringResource(R.string.education_university),
        stringResource(R.string.education_other)
    )
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value, onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(id = R.string.label_education_level)) },
            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onValueChange(option); expanded = false })
            }
        }
    }
}