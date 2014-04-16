SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `schedule` ;
CREATE SCHEMA IF NOT EXISTS `schedule` DEFAULT CHARACTER SET utf8 COLLATE utf8_bin ;
USE `schedule` ;

-- -----------------------------------------------------
-- Table `appointment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `appointment` ;

CREATE TABLE IF NOT EXISTS `appointment` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ext_id` VARCHAR(255) NULL,
  `organizer` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `ext_id_UNIQUE` (`ext_id` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `event`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `event` ;

CREATE TABLE IF NOT EXISTS `event` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `mailbox` VARCHAR(255) NOT NULL,
  `start` BIGINT NOT NULL,
  `end` BIGINT NOT NULL,
  `ext_id` VARCHAR(255) NULL,
  `appointment_id` INT UNSIGNED NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_event_mailbox_time` (`mailbox` ASC, `start` ASC, `end` ASC),
  INDEX `idx_event_ext_id` (`ext_id` ASC),
  INDEX `fk_event_appointment_id_idx` (`appointment_id` ASC),
  CONSTRAINT `fk_event_appointment_id`
    FOREIGN KEY (`appointment_id`)
    REFERENCES `appointment` (`id`)
    ON DELETE SET NULL
    ON UPDATE SET NULL)
ENGINE = InnoDB;

SET SQL_MODE = '';
GRANT USAGE ON *.* TO gobook@localhost;
 DROP USER gobook@localhost;
SET SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
CREATE USER 'gobook'@'localhost' IDENTIFIED BY 'koobog';

GRANT INDEX, DELETE, INSERT, SELECT, UPDATE, TRIGGER ON TABLE schedule.* TO 'gobook'@'localhost';

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
