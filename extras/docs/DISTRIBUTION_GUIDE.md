# How to Sell Your Coffee Shop App

Congratulations! Your app is now fully portable. You can sell it to anyone, and it will work on their computer without any complex setup.

## What You Have
1.  **CoffeeShopApp.jar**: This is your main product. It is located in the `target` folder (look for `inventory-system-1.0-SNAPSHOT.jar`).
2.  **Embedded Database**: The app now uses SQLite. When your customer runs the app for the first time, it will **automatically create** the database file (`coffee_db.sqlite`) and set up all the tables. They don't need to do anything!

## Steps to Package for Sale

1.  **Create a Folder**: Name it `CoffeeShopManager`.
2.  **Copy the JAR**: Copy `target/inventory-system-1.0-SNAPSHOT.jar` into this folder. Rename it to `CoffeeShopApp.jar`.
3.  **Create a "Run" Script (Optional but nice)**:
    *   Create a text file named `StartApp.bat` in the folder.
    *   Write this inside: `start javaw -jar CoffeeShopApp.jar`
    *   Save it. This gives them a double-clickable file that launches the app without a black console window.
4.  **Zip It Up**: Right-click the `CoffeeShopManager` folder -> Send to -> Compressed (zipped) folder.

## What to Deliver to the Customer
Send them the **Zip file**.
Tell them:
1.  Unzip the folder.
2.  Double-click `StartApp.bat` (or the .jar file).
3.  Login with:
    *   **Admin**: `admin` / `admin123`
    *   **Staff**: `service` / `service123`

## Important Note
The database file `coffee_db.sqlite` will appear in the same folder after the first run. If they want to backup their data, they just need to copy that file. Simple!
