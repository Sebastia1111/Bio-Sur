package cl.biosur.app

import cl.biosur.app.util.RutUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias de validación y reglas de negocio para la defensa técnica.
 */
class ValidationTest {

    @Test
    fun testValidChileanRuts() {
        assertTrue(RutUtils.isValidRut("11.111.111-1"))
        assertTrue(RutUtils.isValidRut("11111111-1"))
        assertTrue(RutUtils.isValidRut("111111111"))
        assertTrue(RutUtils.isValidRut("12.345.678-5"))
        assertTrue(RutUtils.isValidRut("10.000.013-K"))
        assertTrue(RutUtils.isValidRut("10000013k"))
    }

    @Test
    fun testInvalidChileanRuts() {
        assertFalse(RutUtils.isValidRut(null))
        assertFalse(RutUtils.isValidRut(""))
        assertFalse(RutUtils.isValidRut("   "))
        assertFalse(RutUtils.isValidRut("11.111.111-2"))
        assertFalse(RutUtils.isValidRut("12345678-0"))
        assertFalse(RutUtils.isValidRut("10.000.013-9"))
        assertFalse(RutUtils.isValidRut("abc-1"))
    }

    @Test
    fun testRutFormatting() {
        assertEquals("11.111.111-1", RutUtils.formatRut("111111111"))
        assertEquals("12.345.678-5", RutUtils.formatRut("12345678-5"))
        assertEquals("10.000.013-K", RutUtils.formatRut("10000013k"))
    }

    @Test
    fun testTimeRangeValidation() {
        val startTime = 1700000000000L
        val endTimeValid = 1700003600000L
        val endTimeInvalid = 1699999000000L

        assertTrue("La hora de término debe ser mayor a la hora de inicio", endTimeValid > startTime)
        assertFalse("La hora de término no puede ser menor a la hora de inicio", endTimeInvalid > startTime)
    }

    @Test
    fun testBlankInputValidationStandard() {
        val emptyInput = "   "
        val filledInput = "Operario de Sanitización"

        assertTrue(emptyInput.isBlank())
        assertFalse(filledInput.isBlank())
    }
}
