package com.example.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

object Translator {
    val ne = mapOf(
        "Dashboard" to "ड्यासबोर्ड", "History" to "इतिहास", "Analytics" to "एनालिटिक्स", 
        "Profile" to "प्रोफाइल", "About" to "हाम्रो बारेमा", "Logout" to "लगआउट",
        "Total Balance" to "कुल ब्यालेन्स", "Income" to "आम्दानी", "Expense" to "खर्च",
        "Recent Transactions" to "भर्खरको कारोबार", "Add Transaction" to "कारोबार थप्नुहोस्",
        "Save Transaction" to "सुरक्षित गर्नुहोस्", "Title" to "शीर्षक", "Amount" to "रकम",
        "Account Settings" to "खाता सेटिङहरू", "Language" to "भाषा", "Currency" to "मुद्रा",
        "Category" to "वर्ग", "Description" to "विवरण", "Note" to "नोट", "Camera" to "क्यामेरा",
        "Gallery" to "ग्यालरी", "Photo attached" to "फोटो संलग्न गरियो", "All" to "सबै",
        "Income vs Expenses" to "आम्दानी विरुद्ध खर्च", "Update Profile Picture" to "प्रोफाइल तस्वीर अपडेट गर्नुहोस्",
        "Old Password" to "पुरानो पासवर्ड", "New Password" to "नयाँ पासवर्ड", "Confirm New Password" to "नयाँ पासवर्ड पुष्टि गर्नुहोस्",
        "Change Password" to "पासवर्ड परिवर्तन गर्नुहोस्", "Smart Income" to "InExa",
        "Version 1.0.0" to "संस्करण १.०.०", "Close" to "बन्द गर्नुहोस्", "Date" to "मिति", "Type" to "प्रकार",
        "7 Days" to "७ दिन", "1 Month" to "१ महिना", "1 Year" to "१ वर्ष",
        "Salary" to "तलब", "Freelancing" to "फ्रीलान्सिङ", "Business" to "व्यापार", "Gift" to "उपहार", "Investment" to "लगानी", "Other" to "अन्य",
        "Food" to "खाना", "Transport" to "यातायात", "Shopping" to "किनमेल", "Education" to "शिक्षा", "Health" to "स्वास्थ्य", "Entertainment" to "मनोरञ्जन", "Bills" to "बिलहरू", "Rent" to "भाडा", "Travel" to "यात्रा",
        "AI Insights" to "एआई इनसाइट्स", "Ask about your finances..." to "आफ्नो वित्तको बारेमा सोध्नुहोस्..."
    )
    
    val es = mapOf(
        "Dashboard" to "Tablero", "History" to "Historial", "Analytics" to "Análisis", 
        "Profile" to "Perfil", "About" to "Acerca de", "Logout" to "Cerrar Sesión",
        "Total Balance" to "Saldo Total", "Income" to "Ingresos", "Expense" to "Gastos",
        "Recent Transactions" to "Transacciones Recientes", "Add Transaction" to "Añadir Transacción",
        "Save Transaction" to "Guardar Transacción", "Title" to "Título", "Amount" to "Cantidad",
        "Account Settings" to "Configuración de la Cuenta", "Language" to "Idioma", "Currency" to "Moneda",
        "Category" to "Categoría", "Description" to "Descripción", "Note" to "Nota", "Camera" to "Cámara",
        "Gallery" to "Galería", "Photo attached" to "Foto adjunta", "All" to "Todo",
        "Income vs Expenses" to "Ingresos vs Gastos", "Update Profile Picture" to "Actualizar Foto de Perfil",
        "Old Password" to "Contraseña Anterior", "New Password" to "Nueva Contraseña", "Confirm New Password" to "Confirmar Contraseña",
        "Change Password" to "Cambiar Contraseña", "Smart Income" to "InExa",
        "Version 1.0.0" to "Versión 1.0.0", "Close" to "Cerrar", "Date" to "Fecha", "Type" to "Tipo",
        "7 Days" to "7 Días", "1 Month" to "1 Mes", "1 Year" to "1 Año",
        "Salary" to "Salario", "Freelancing" to "Freelance", "Business" to "Negocios", "Gift" to "Regalo", "Investment" to "Inversión", "Other" to "Otro",
        "Food" to "Comida", "Transport" to "Transporte", "Shopping" to "Compras", "Education" to "Educación", "Health" to "Salud", "Entertainment" to "Entretenimiento", "Bills" to "Facturas", "Rent" to "Alquiler", "Travel" to "Viaje",
        "AI Insights" to "Insights de IA", "Ask about your finances..." to "Pregunta sobre tus finanzas..."
    )

    val fr = mapOf(
        "Dashboard" to "Tableau", "History" to "Historique", "Analytics" to "Analytique", 
        "Profile" to "Profil", "About" to "À Propos", "Logout" to "Déconnexion",
        "Total Balance" to "Solde Total", "Income" to "Revenus", "Expense" to "Dépenses",
        "Recent Transactions" to "Transactions Récentes", "Add Transaction" to "Ajouter",
        "Save Transaction" to "Enregistrer", "Title" to "Titre", "Amount" to "Montant",
        "Account Settings" to "Paramètres du Compte", "Language" to "Langue", "Currency" to "Devise",
        "Category" to "Catégorie", "Description" to "Description", "Note" to "Note", "Camera" to "Caméra",
        "Gallery" to "Galerie", "Photo attached" to "Photo jointe", "All" to "Tout",
        "Income vs Expenses" to "Revenus vs Dépenses", "Update Profile Picture" to "Mettre à jour la photo",
        "Old Password" to "Ancien MDP", "New Password" to "Nouveau MDP", "Confirm New Password" to "Confirmer MDP",
        "Change Password" to "Changer le Mot de Passe", "Smart Income" to "InExa",
        "Version 1.0.0" to "Version 1.0.0", "Close" to "Fermer", "Date" to "Date", "Type" to "Type",
        "7 Days" to "7 Jours", "1 Month" to "1 Mois", "1 Year" to "1 An",
        "Salary" to "Salaire", "Freelancing" to "Freelance", "Business" to "Entreprise", "Gift" to "Cadeau", "Investment" to "Investissement", "Other" to "Autre",
        "Food" to "Nourriture", "Transport" to "Transport", "Shopping" to "Shopping", "Education" to "Éducation", "Health" to "Santé", "Entertainment" to "Divertissement", "Bills" to "Factures", "Rent" to "Loyer", "Travel" to "Voyage"
    )

    val hi = mapOf(
        "Dashboard" to "डैशबोर्ड", "History" to "इतिहास", "Analytics" to "विश्लेषण", 
        "Profile" to "प्रोफ़ाइल", "About" to "के बारे में", "Logout" to "लॉग आउट",
        "Total Balance" to "कुल शेषफल", "Income" to "आय", "Expense" to "खर्च",
        "Recent Transactions" to "हाल के लेनदेन", "Add Transaction" to "लेनदेन जोड़ें",
        "Save Transaction" to "लेनदेन सहेजें", "Title" to "शीर्षक", "Amount" to "राशि",
        "Account Settings" to "खाता सेटिंग्स", "Language" to "भाषा", "Currency" to "मुद्रा",
        "Category" to "श्रेणी", "Description" to "विवरण", "Note" to "टिप्पणी", "Camera" to "कैमरा",
        "Gallery" to "गैलरी", "Photo attached" to "फोटो संलग्न है", "All" to "सभी",
        "Income vs Expenses" to "आय बनाम व्यय", "Update Profile Picture" to "प्रोफ़ाइल तस्वीर अपडेट करें",
        "Old Password" to "पुराना पासवर्ड", "New Password" to "नया पासवर्ड", "Confirm New Password" to "पासवर्ड की पुष्टि करें",
        "Change Password" to "पासवर्ड बदलें", "Smart Income" to "InExa",
        "Version 1.0.0" to "संस्करण 1.0.0", "Close" to "बंद करें", "Date" to "तारीख", "Type" to "प्रकार",
        "7 Days" to "7 दिन", "1 Month" to "1 महीना", "1 Year" to "1 वर्ष",
        "Salary" to "वेतन", "Freelancing" to "फ्रीलांसिंग", "Business" to "व्यापार", "Gift" to "उपहार", "Investment" to "निवेश", "Other" to "अन्य",
        "Food" to "भोजन", "Transport" to "परिवहन", "Shopping" to "खरीदारी", "Education" to "शिक्षा", "Health" to "स्वास्थ्य", "Entertainment" to "मनोरंजन", "Bills" to "बिल", "Rent" to "किराया", "Travel" to "यात्रा"
    )
}

@Composable
fun trans(text: String): String {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
    val lang = prefs.getString("language", "English(US)")
    return when (lang) {
        "Nepali" -> Translator.ne[text] ?: text
        "Spanish" -> Translator.es[text] ?: text
        "French" -> Translator.fr[text] ?: text
        "Hindi" -> Translator.hi[text] ?: text
        else -> text
    }
}
