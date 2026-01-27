package brava.fightinwords.ui.typesetter

import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

/**
 * TODO: This is still quite janky, and not yet ready to replace [TypesetterButtonsVie2w].
 */
@Composable
fun TypesetterButtonsMenu(
    typesetterButtons: TypesetterButtons,
    initiallyExpanded: Boolean = false,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    if (expanded == false) {
        Button(onClick = { expanded = true }) {
            Text("Sorting")
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        SortButton.entries.forEach {
            SortButton(it, typesetterButtons.onSortButtonClick)
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun DropdownPreview() {
    TypesetterButtonsMenu(
        typesetterButtons = TypesetterButtons(
            {},
            {},
            {},
            {}
        ),
        initiallyExpanded = false
    )
}

@Preview(showSystemUi = true)
@Composable
fun DropdownPreview_Expanded() {
    TypesetterButtonsMenu(
        typesetterButtons = TypesetterButtons(
            {},
            {},
            {},
            {}
        ),
        initiallyExpanded = true
    )
}

