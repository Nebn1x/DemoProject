// @ts-ignore
import { test, expect } from '@playwright/test';

/**
 * E2E тести Mock-It - повний користувацький сценарій.
 * Потребують запущених бекенду (8080) + фронту (5173) + Docker (Postgres/Redis).
 * Запуск: npx playwright test
 */

// унікальний email для кожного прогону (щоб не конфліктувати з попередніми)
const uniqueEmail = () => `e2e_${Date.now()}@test.com`;
const PASSWORD = 'password123';

test.describe('Mock-It E2E', () => {

  // @ts-ignore
  test('реєстрація нового користувача → потрапляє на дашборд', async ({ page }) => {
    await page.goto('/register');

    await page.fill('input[type="email"]', uniqueEmail());
    await page.fill('input[type="password"]', PASSWORD);
    await page.click('button:has-text("Зареєструватися")');

    // після реєстрації - дашборд з заголовком Mock-It
    await expect(page.locator('text=Mock-It')).toBeVisible({ timeout: 10000 });
    await expect(page.locator('text=Мої ендпоінти')).toBeVisible();
  });

  // @ts-ignore
  test('повний цикл: реєстрація → створення ендпоінта → поява в таблиці', async ({ page }) => {
    // 1. Реєстрація
    await page.goto('/register');
    await page.fill('input[type="email"]', uniqueEmail());
    await page.fill('input[type="password"]', PASSWORD);
    await page.click('button:has-text("Зареєструватися")');
    await expect(page.locator('text=Мої ендпоінти')).toBeVisible({ timeout: 10000 });

    // 2. Відкриваємо модалку створення
    await page.click('button:has-text("Створити ендпоінт")');
    await expect(page.locator('text=Новий ендпоінт')).toBeVisible();

    // 3. Заповнюємо шлях (унікальний)
    const path = `/api/e2e-${Date.now()}`;
    await page.fill('input[placeholder="/api/example"]', path);

    // 4. Створюємо
    await page.click('button:has-text("Створити"):not(:has-text("ендпоінт"))');

    // 5. З'являється екран успіху з URL
    await expect(page.locator('text=Ендпоінт створено')).toBeVisible({ timeout: 10000 });

    // 6. Закриваємо
    await page.click('button:has-text("Готово")');

    // 7. Ендпоінт є в таблиці
    await expect(page.locator(`text=${path}`)).toBeVisible();
  });

  // @ts-ignore
  test('логін існуючого користувача', async ({ page }) => {
    // спершу реєструємо
    const email = uniqueEmail();
    await page.goto('/register');
    await page.fill('input[type="email"]', email);
    await page.fill('input[type="password"]', PASSWORD);
    await page.click('button:has-text("Зареєструватися")');
    await expect(page.locator('text=Мої ендпоінти')).toBeVisible({ timeout: 10000 });

    // виходимо
    await page.click('button:has-text("Вийти")');
    await expect(page).toHaveURL(/.*login/);

    // логінимось тим самим
    await page.fill('input[type="email"]', email);
    await page.fill('input[type="password"]', PASSWORD);
    await page.click('button:has-text("Увійти")');

    // знову на дашборді
    await expect(page.locator('text=Мої ендпоінти')).toBeVisible({ timeout: 10000 });
  });

  // @ts-ignore
  test('сесія зберігається після перезавантаження сторінки', async ({ page }) => {
    await page.goto('/register');
    await page.fill('input[type="email"]', uniqueEmail());
    await page.fill('input[type="password"]', PASSWORD);
    await page.click('button:has-text("Зареєструватися")');
    await expect(page.locator('text=Мої ендпоінти')).toBeVisible({ timeout: 10000 });

    // F5
    await page.reload();

    // лишаємось залогіненими (не викинуло на /login)
    await expect(page.locator('text=Мої ендпоінти')).toBeVisible({ timeout: 10000 });
    await expect(page).not.toHaveURL(/.*login/);
  });

});
