package org.ibs;


import org.ibs.pages.FoodPage;
import org.ibs.utils.Locators;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.*;
import java.time.Duration;

public class QualitDBTestCase {
    Connection connection;
    private WebDriver driver;
    private FoodPage foodPage;
    private WebDriverWait webDriverWait;

    /**
     * Метод, выполняющийся перед каждым тестом. Инициализирует WebDriver,  подключается к базе данных и открывает страницу со списком продуктов.
     * @throws SQLException если возникает ошибка при подключении к базе данных.
     */
    @BeforeEach
    void testsPreCondition() throws SQLException {
        System.setProperty("webdriver.chromedriver.driver","src/test/resources/chrome.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        driver.get("http://localhost:8080/food");
        webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        foodPage = new FoodPage(driver);
        connection = DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/mem:testdb","user","pass");
    }

    /**
     * Параметризованный тест для проверки добавления овощей.
     * <p>
     * Шаги выполнения:
     * 1) Выполняется запрос на наличие записи с заданными параметрами в базе данных.
     * 2) Нажимается кнопка "Добавить".
     * 3) Заполняются поля "Название", "Тип" и "Экзотичность".
     * 4) Нажимается кнопка "Сохранить".
     * 5) Выполняется запрос на наличие в базе данных новой записи с заданным наименованием
     * 6) Выполняется проверка результата запроса:
     *      6.1 Проверка количества строк в ответе
     *      6.2 Проверка типа товара в строке ответа
     *      6.3 Проверка экзотичности товара в строке ответа
     * 7) Удаление добавленной строки из базы данных
     * @param name Наименование товара.
     * @param type Тип товара (для данного теста всегда "Овощ").
     * @param exotic Признак экзотичности товара.
     * @throws SQLException если возникает ошибка при работе с базой данных.
     */
    @ParameterizedTest
    @CsvSource({"Melotria,Овощ,true","Картофель,Овощ,false"})
    void testAddVegetable(String name, String type, boolean exotic) throws SQLException {
        // Проверка на существование записи в базе данных
        String query_check_existing = "SELECT COUNT(FOOD_ID) FROM FOOD WHERE FOOD_NAME = ? AND FOOD_TYPE = ? AND FOOD_EXOTIC = ?";
        PreparedStatement existing_PS = connection.prepareStatement(query_check_existing);
        existing_PS.setString(1,name);
        existing_PS.setString(2, "VEGETABLE");
        int exotic_code = exotic? 1:0;
        existing_PS.setInt(3, exotic_code);
        ResultSet resultSet = existing_PS.executeQuery();
        resultSet.first();
        int count = resultSet.getInt("COUNT(FOOD_ID)");
        Assertions.assertEquals(0, count,"Строка уже существует БД");

        // Добавление товара
        int initialRowCount = foodPage.getTableRows().size();
        foodPage = foodPage.clickAddBtn()
                .fillNameField(name)
                .setType(type)
                .setExotic(exotic)
                .clickSaveBtn();
        webDriverWait.until(ExpectedConditions.numberOfElementsToBe(Locators.FoodPage.TABLE_ROWS, initialRowCount + 1));

        //Проверка, что товар добавился в базу данных
        String query_check_adding = "SELECT FOOD_TYPE, FOOD_EXOTIC FROM FOOD WHERE FOOD_NAME = ?";
        PreparedStatement adding_PS = connection.prepareStatement(query_check_adding);
        adding_PS.setString(1, name);
        ResultSet resultSet1 = adding_PS.executeQuery();
        if (resultSet1.next()){
            String type_db = resultSet1.getString("FOOD_TYPE");
            int exotic_db = resultSet1.getInt("FOOD_EXOTIC");
            Assertions.assertAll("Проверка добавленной строки",
                    () -> Assertions.assertEquals("VEGETABLE", type_db, "Тип товара не соответствует ожидаемому"),
                    () -> Assertions.assertEquals(exotic_code, exotic_db, "Экзотичность товара не соответствует ожидаемой"));
        }else{
            Assertions.fail("Строка не добавилась в БД");
        }

        //Удаление добавленной строки из базы данных
        String delete_query = "DELETE FROM FOOD WHERE FOOD_NAME = ?";
        PreparedStatement delete_PS = connection.prepareStatement(delete_query);
        delete_PS.setString(1, name);
        int rows = delete_PS.executeUpdate();
        Assertions.assertEquals(1, rows, "Ошибка при удалении данных");
    }
    /**
     * Параметризованный тест для проверки добавления фруктов. Аналогичен testAddVegetable.
     * <p>
     * Шаги выполнения:
     * 1) Выполняется запрос на наличие записи с заданными параметрами в базе данных.
     * 2) Нажимается кнопка "Добавить".
     * 3) Заполняются поля "Название", "Тип" и "Экзотичность".
     * 4) Нажимается кнопка "Сохранить".
     * 5) Выполняется запрос на наличие в базе данных новой записи с заданным наименованием
     * 6) Выполняется проверка результата запроса:
     *      6.1 Проверка количества строк в ответе
     *      6.2 Проверка типа товара в строке ответа
     *      6.3 Проверка экзотичности товара в строке ответа
     * 7) Удаление добавленной строки из базы данных
     *
     * @param name Наименование товара.
     * @param type Тип товара (для данного теста всегда "Фрукт").
     * @param exotic Признак экзотичности товара.
     * @throws SQLException если возникает ошибка при работе с базой данных.
     */
    @ParameterizedTest
    @CsvSource({"Клубника,Фрукт,false","Mangosteen,Фрукт,true"})
    void testAddFruit(String name, String type, boolean exotic) throws SQLException {
        // Проверка на существование записи в базе данных
        String query_check_existing = "SELECT COUNT(FOOD_ID) FROM FOOD WHERE FOOD_NAME = ? AND FOOD_TYPE = ? AND FOOD_EXOTIC = ?";
        PreparedStatement existing_PS = connection.prepareStatement(query_check_existing);
        existing_PS.setString(1,name);
        existing_PS.setString(2, "FRUIT");
        int exotic_code = exotic? 1:0;
        existing_PS.setInt(3, exotic_code);
        ResultSet resultSet = existing_PS.executeQuery();
        resultSet.first();
        int count = resultSet.getInt("COUNT(FOOD_ID)");
        Assertions.assertEquals(0, count,"Строка уже существует БД");

        // Добавление товара
        int initialRowCount = foodPage.getTableRows().size();
        foodPage = foodPage.clickAddBtn()
                .fillNameField(name)
                .setType(type)
                .setExotic(exotic)
                .clickSaveBtn();
        webDriverWait.until(ExpectedConditions.numberOfElementsToBe(Locators.FoodPage.TABLE_ROWS, initialRowCount + 1));

        //Проверка, что товар добавился в базу данных
        String query_check_adding = "SELECT FOOD_TYPE, FOOD_EXOTIC FROM FOOD WHERE FOOD_NAME = ?";
        PreparedStatement adding_PS = connection.prepareStatement(query_check_adding);
        adding_PS.setString(1, name);
        ResultSet resultSet1 = adding_PS.executeQuery();
        if (resultSet1.next()){
            String type_db = resultSet1.getString("FOOD_TYPE");
            int exotic_db = resultSet1.getInt("FOOD_EXOTIC");
            Assertions.assertAll("Проверка добавленной строки",
                    () -> Assertions.assertEquals("FRUIT", type_db, "Тип товара не соответствует ожидаемому"),
                    () -> Assertions.assertEquals(exotic_code, exotic_db, "Экзотичность товара не соответствует ожидаемой"));
        }else{
            Assertions.fail("Строка не добавилась в БД");
        }

        //Удаление добавленной строки из базы данных
        String delete_query = "DELETE FROM FOOD WHERE FOOD_NAME = ?";
        PreparedStatement delete_PS = connection.prepareStatement(delete_query);
        delete_PS.setString(1, name);
        int rows = delete_PS.executeUpdate();
        Assertions.assertEquals(1, rows, "Ошибка при удалении данных");
    }

    /**
     * Тестирует добавление уже существующего продукта через UI.  Проверяет, что система корректно обрабатывает попытку добавления дубликата.
     * Тест предполагает наличие предварительно созданной записи в базе данных.
     *<p>
     * Шаги теста:
     * 1) Проверяется, что запись с заданным именем, типом и экзотичностью отсутствует в базе данных.
     * 2) Создается запись в базе данных с помощью SQL запроса, имитируя добавление через UI.
     * 3) Добавляется товар с теми же параметрами через пользовательский интерфейс (UI).
     * 4) Проверяется наличие дубликатов в базе данных.  Ожидается, что будет найдено две записи с одинаковым именем, вызывая исключение {@link DuplicateProductException}.
     * 5) Проверяется, что сообщение об исключении содержит слово "Дубликат".
     * 6) Удаляются обе добавленные записи из базы данных.
     * 7) Проверяется, что обе записи были успешно удалены.
     *
     * @param name   Название продукта.
     * @param type   Тип продукта ("Фрукт" в данном случае).
     * @param exotic Флаг, указывающий на экзотичность продукта (true/false).
     * @throws SQLException Если возникает ошибка при взаимодействии с базой данных.
     */
    @ParameterizedTest
    @CsvSource({"Виноград,Фрукт,false"})
    void testAddExistingProduct(String name, String type, boolean exotic) throws SQLException {
        // Проверка на существование записи в базе данных
        String query_check_existing = "SELECT COUNT(FOOD_ID) FROM FOOD WHERE FOOD_NAME = ? AND FOOD_TYPE = ? AND FOOD_EXOTIC = ?";
        PreparedStatement existing_PS = connection.prepareStatement(query_check_existing);
        existing_PS.setString(1,name);
        existing_PS.setString(2, "FRUIT");
        int exotic_code = exotic? 1:0;
        existing_PS.setInt(3, exotic_code);
        ResultSet resultSet = existing_PS.executeQuery();
        resultSet.first();
        int count = resultSet.getInt("COUNT(FOOD_ID)");
        Assertions.assertEquals(0, count,"Строка уже существует БД");

        //Добавление строки в базу данных
        String insert_query = "INSERT INTO FOOD VALUES (DEFAULT,?,'FRUIT',?)";
        PreparedStatement insert_PS = connection.prepareStatement(insert_query);
        insert_PS.setString(1, name);
        insert_PS.setInt(2, exotic_code);
        insert_PS.executeUpdate();

        //Добавление товара через UI
        int initialRowCount = foodPage.getTableRows().size();
        foodPage = foodPage.clickAddBtn()
                .fillNameField(name)
                .setType(type)
                .setExotic(exotic)
                .clickSaveBtn();
        webDriverWait.until(ExpectedConditions.numberOfElementsToBe(Locators.FoodPage.TABLE_ROWS, initialRowCount + 1));

        //Проверка на наличие дубликатов в базе данных
        String query_check_adding = "SELECT COUNT(*) FROM FOOD WHERE FOOD_NAME = ?";
        PreparedStatement adding_PS = connection.prepareStatement(query_check_adding);
        adding_PS.setString(1, name);
        ResultSet resultSet1 = adding_PS.executeQuery();
        resultSet1.first();

        DuplicateProductException exception = Assertions.assertThrows(DuplicateProductException.class, () ->{
            int count_duple = resultSet1.getInt(1);
            if (count_duple == 2){
                throw new DuplicateProductException("Дубликат товара найден в базе данных");
            }
        });
        Assertions.assertTrue(exception.getMessage().contains("Дубликат"));

        //Удаление добавленных строк из базы данных
        String delete_query = "DELETE FROM FOOD WHERE FOOD_NAME = ?";
        PreparedStatement delete_PS = connection.prepareStatement(delete_query);
        delete_PS.setString(1, name);
        int rows = delete_PS.executeUpdate();
        Assertions.assertEquals(2, rows, "Ошибка при удалении данных");
    }

    /**
     *  Пользовательское исключение, которое выбрасывается, если обнаружен дубликат товара.
     */
    static class DuplicateProductException extends Exception {
        public DuplicateProductException(String message) {
            super(message);
        }
    }

    /**
     * Метод, выполняющийся после каждого теста. Сбрасывает данные в БД, закрывает браузер и соединение с БД.
     */
    @AfterEach
    void postCondition() {
        driver.quit();
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
