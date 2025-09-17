package com.designpattern.core.element;

import com.designpattern.strategy.element.ElementInteractionManager;
import com.designpattern.utils.LogUtils;
import com.designpattern.utils.exceptions.FrameworkException;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced WebElement wrapper that provides robust interaction methods with
 * built-in waits, retries, and fallback mechanisms.
 */