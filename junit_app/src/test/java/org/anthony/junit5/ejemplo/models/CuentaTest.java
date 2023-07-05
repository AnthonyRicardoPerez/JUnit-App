package org.anthony.junit5.ejemplo.models;


import org.anthony.junit5.ejemplo.exceptions.DineroInsuficienteExcep;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
class CuentaTest {

    Cuenta cuenta;
    @BeforeEach
    void initMetodoTest(TestInfo testInfo,TestReporter testReporter){
        this.cuenta = new Cuenta("Anthony", new BigDecimal("1000.12345"));
        System.out.println("Iniciando el metodo");
        System.out.println("ejecutando: " + testInfo.getDisplayName()+ " " + testInfo.getTestMethod().orElse(null).getName() );
    }

    @AfterEach
    void tearDown(){
        System.out.println("Finalizando el metodo de prueba");
    }

    @BeforeAll
    static void beforeAll(){
        System.out.println("Iniciando el test");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Finalizando el test");
    }


    @Nested
    @Tag("cuenta")
    @DisplayName("probando atributos de la cuenta corriente")
    class CuentaTestNombreSaldo{

        @Test
        @DisplayName("Probando el nombre ")
        void testNombreCuenta() {

            //cuenta.setPersona("Anthony");
            String esperado = "Anthony";
            String real = cuenta.getPersona();
            assertNotNull(real,()-> "La cuenta no puede ser null");
            assertEquals(esperado, real,()-> "El nombre no es el que se esperaba");
        }

        @Test
        @DisplayName("Probando el saldo ")
        void testSaldoCuenta() {

            assertEquals(1000.12345, cuenta.getSaldo().doubleValue(),()->"El valor no es el esperado");
            assertNotNull(cuenta.getSaldo(), ()->"La cuenta no debe de ser null");
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0,()->"El saldo no debe ser negativo");
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0, ()->"El saldo de la cuenta tiene que ser mayor que cero");
        }

        @Test
        @DisplayName("Testeando referencias que sean iguales")
        void testReferenciaCuenta() {
            cuenta = new Cuenta("Anthony", new BigDecimal("8900.997"));
            Cuenta cuenta2 = new Cuenta("Anthony", new BigDecimal("8900.997"));

            // assertNotEquals(cuenta2, cuenta);
            assertEquals(cuenta2, cuenta);
        }

    }

    @Nested
    @Tag("cuenta")
    @Tag("banco")
    class operacionesTest{

        @Test
        @DisplayName("testeando transaccion debito de la cuenta corriente")
        void testDebitoCuenta() {
            cuenta = new Cuenta("Anthony", new BigDecimal("1000.12345"));
            cuenta.debito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo(),()->"El saldo no debe de ser null");
            assertEquals(900, cuenta.getSaldo().intValue(),()->"El valor no es el esperado ");
            assertEquals("900.12345", cuenta.getSaldo().toPlainString(),()->"El valor no es el esperado ");
        }

        @Test
        @DisplayName("testeando transaccion credito de la cuenta corriente\"")
        void testCreditoCuenta() {
            cuenta = new Cuenta("Anthony", new BigDecimal("1000.12345"));
            cuenta.credito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo(),()->"El saldo no puede ser null");
            assertEquals(1100, cuenta.getSaldo().intValue(),()->"El valor no es el esperado ");
            assertEquals("1100.12345", cuenta.getSaldo().toPlainString(),()->"El valor no es el esperado ");
        }
        @Test
        @DisplayName("Test para transferir dinero de cuenta a cuenta")
        void testTransferirDineroCuenta() {
            Cuenta cuenta1 = new Cuenta("Anthony Ricardo", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("Andy Garcia ", new BigDecimal("1500.9090"));

            Banco banco = new Banco();
            banco.setNombre("Banco Internacional");
            banco.transferir(cuenta2, cuenta1, new BigDecimal(500));

            assertEquals("1000.9090", cuenta2.getSaldo().toPlainString(),()->"El valor no es el esperado ");
            assertEquals("3000", cuenta1.getSaldo().toPlainString(),()->"El valor no es el esperado ");
        }

    }



    @Test
    @Tag("cuenta")
    @Tag("error")
    @DisplayName("Testeando si la cuenta tiene dinero insuficiente manejando excepciones")
    void testDineroInsuficienteExceptionCuenta() {
        cuenta = new Cuenta("Anthony", new BigDecimal("1000.12345"));

        Exception exception = assertThrows(DineroInsuficienteExcep.class, () -> {
            cuenta.debito(new BigDecimal(1500));
        });
        String actual = exception.getMessage();
        String esperado = "Dinero Insuficiente";
        assertEquals(esperado, actual,()->"El valor no es el esperado ");
    }


    @Test
    @Tag("cuenta")
    @Tag("banco")
    @DisplayName("Testeando relaciones entre las cuentas y el banco")
    void testRealcionBancoCuentas() {
        Cuenta cuenta1 = new Cuenta("Anthony Ricardo", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("Andy Garcia ", new BigDecimal("1500.9090"));

        Banco banco = new Banco();
        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);

        banco.setNombre("Banco Internacional");
        banco.transferir(cuenta2, cuenta1, new BigDecimal(500));

        assertAll(() -> assertEquals("1000.9090", cuenta2.getSaldo().toPlainString(),()->"El valor no es el esperado"),

                () -> assertEquals("3000", cuenta1.getSaldo().toPlainString(),()->"El valor no es el esperado"),

                () -> assertEquals(2, banco.getCuentas().size(),()->"El banco no tiene las cuentas esperadas"),

                () -> assertEquals("Banco Internacional", cuenta1.getBanco().getNombre(),()->"El nombre del banco de la cuenta 1 no es el esperado "),

                () -> {
                    assertEquals("Anthony Ricardo", banco.getCuentas().stream()
                            .filter(c -> c.getPersona().equals("Anthony Ricardo"))
                            .findFirst().get().getPersona());
                },
                () -> {
                    assertTrue(banco.getCuentas().stream()
                            .filter(c -> c.getPersona().equals("Anthony Ricardo"))
                            .findFirst().isPresent());
                });
    }

    @Nested
    @Tag("SO")
    class testWindows{

        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testSoloWindows() {

        }

        @Test
        @DisabledOnOs({OS.MAC,OS.LINUX})
        void SoloLinuxMac() {
        }

    }

    @Test
    @Tag("cuenta")
    @Tag("banco")
    @DisplayName("Usando Assumptions")
    void testSaldoCuentaDev() {

        boolean esDev ="DEV".equals(System.getProperty("ENV"));
        assumeTrue(esDev);

        assertEquals(1000.12345, cuenta.getSaldo().doubleValue(),()->"El valor no es el esperado");
        assertNotNull(cuenta.getSaldo(), ()->"La cuenta no debe de ser null");
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0,()->"El saldo no debe ser negativo");
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0, ()->"El saldo de la cuenta tiene que ser mayor que cero");
    }

    @Tag("param")
    @ParameterizedTest(name="numero {index} ejecutando con valor {0}")
    @ValueSource(strings = {"100","200","300","500","700","1000"})
    void testDebitoCuenta(String monto) {
        cuenta = new Cuenta("Anthony", new BigDecimal("1000.12345"));
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo(),()->"El saldo no debe de ser null");
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO)>0);
    }

    @Nested
    @Tag("timeout")
    public class TimeOutTest{

        @Test
        @Timeout(5)
        void pruebaTimeOut() throws InterruptedException {
            TimeUnit.SECONDS.sleep(6);
        }

        @Test
        @Timeout(value=500, unit = TimeUnit.MILLISECONDS)
        void pruebaTimeOut2() throws InterruptedException {
            TimeUnit.SECONDS.sleep(6);
        }

        @Test
        void testTimeOutAssertions() {

            assertTimeout(Duration.ofSeconds(5), ()->{
                TimeUnit.MILLISECONDS.sleep(4500);
            });
        }
    }

}