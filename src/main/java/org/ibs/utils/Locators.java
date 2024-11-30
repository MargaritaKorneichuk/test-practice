package org.ibs.utils;

import org.openqa.selenium.By;

public interface Locators {
    interface FoodPage{
        By BTN_ADD = By.xpath("//div[@class = \"btn-grou mt-2 mb-2\"]/button");
        By BTN_SAVE = By.xpath("//button[@id=\"save\"]");
        By INPUT_NAME = By.xpath("//input[@id=\"name\"]");
        By SELECT_TYPE = By.xpath("//select[@id=\"type\"]");
        By INPUT_EXOTIC = By.xpath("//input[@id=\"exotic\"]");
        By TABLE_ROWS = By.xpath("//table/tbody/tr");
        By NAVBAR_DROPDOWN = By.xpath("//a[@id=\"navbarDropdown\"]");
        By BTN_RESET = By.xpath("//a[@id=\"reset\"]");
        By TABLE_ROW_NAME = By.xpath("./td[1]");
        By TABLE_ROW_TYPE = By.xpath("./td[2]");
        By TABLE_ROW_EXOTIC = By.xpath("./td[3]");
    }
}
