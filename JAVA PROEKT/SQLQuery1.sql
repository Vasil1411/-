-- 1. Създаване на базата данни
CREATE DATABASE AutoMasterDB;
GO

USE AutoMasterDB;
GO

-- 2. Таблица "Клиенти" (Clients)
CREATE TABLE clients (
    id INT IDENTITY(1,1) PRIMARY KEY,
    first_name NVARCHAR(50) NOT NULL,
    last_name NVARCHAR(50) NOT NULL,    
    phone NVARCHAR(20) NOT NULL,
    email NVARCHAR(100)
);

-- 3. Таблица "Автомобили" (Vehicles)
CREATE TABLE vehicles (
    id INT IDENTITY(1,1) PRIMARY KEY,
    reg_number NVARCHAR(15) UNIQUE NOT NULL, 
    brand_model NVARCHAR(100) NOT NULL,
    client_id INT NOT NULL,
    CONSTRAINT FK_Vehicles_Clients FOREIGN KEY (client_id) 
        REFERENCES clients(id) ON DELETE CASCADE
);

-- 4. Таблица "Услуги" (Services) - Ценоразпис
CREATE TABLE services (
    id INT IDENTITY(1,1) PRIMARY KEY,
    service_name NVARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

-- 5. Таблица "Ремонти" (Repairs) - Свързваща таблица
CREATE TABLE repairs (
    id INT IDENTITY(1,1) PRIMARY KEY,
    repair_date DATE NOT NULL DEFAULT GETDATE(),
    status NVARCHAR(20) NOT NULL, -- Опции: Приет, В ремонт, Готов
    vehicle_id INT NOT NULL,
    service_id INT NOT NULL,
    CONSTRAINT FK_Repairs_Vehicles FOREIGN KEY (vehicle_id) 
        REFERENCES vehicles(id) ON DELETE CASCADE,
    CONSTRAINT FK_Repairs_Services FOREIGN KEY (service_id) 
        REFERENCES services(id) -- Тук няма CASCADE, за да пазим историята на цените
);