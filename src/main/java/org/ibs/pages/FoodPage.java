package org.ibs.pages;

import org.ibs.utils.Locators;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class FoodPage {
    private final WebDriver driver;

    public FoodPage(WebDriver driver) {
        this.driver = driver;
    }

    public FoodPage clickAddBtn(){
        WebElement btnAdd = driver.findElement(Locators.FoodPage.BTN_ADD);
        btnAdd.click();
        return this;
    }
    public FoodPage clickSaveBtn(){
        WebElement btnSave = driver.findElement(Locators.FoodPage.BTN_SAVE);
        btnSave.click();
        return this;
    }
    public void clickResetBtn(){
        WebElement btnResetBtn = driver.findElement(Locators.FoodPage.BTN_RESET);
        btnResetBtn.click();
    }
    public FoodPage clickNavBarDropDown(){
        WebElement navbarDropdown = driver.findElement(Locators.FoodPage.NAVBAR_DROPDOWN);
        navbarDropdown.click();
        return this;
    }
    public List<WebElement> getTableRows(){
        return driver.findElements(Locators.FoodPage.TABLE_ROWS);
    }

    public FoodPage fillNameField(String name){
        WebElement nameField = driver.findElement(Locators.FoodPage.INPUT_NAME);
        nameField.sendKeys(name);
        return this;
    }

    public FoodPage setType(String type){
        WebElement selectType = driver.findElement(Locators.FoodPage.SELECT_TYPE);
        Select select = new Select(selectType);
        select.selectByVisibleText(type);
        return this;
    }
    public FoodPage setExotic(boolean exotic){
        WebElement inputExotic = driver.findElement(Locators.FoodPage.INPUT_EXOTIC);
        if (exotic){
            inputExotic.click();
        }
        return this;
    }
}
