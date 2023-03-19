DROP TABLE IF EXISTS `goods_stock`;
CREATE TABLE `goods_stock`
(
    `goods_id`   INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `goods_name` VARCHAR(255)     NOT NULL DEFAULT '',
    `balance`    INT(11)          NOT NULL,
    `sales`      INT(11)          NOT NULL,
    `ver`        INT(11)          NOT NULL COMMENT 'Lock with version',
    PRIMARY KEY (`goods_id`)
) ENGINE = INNODB
  AUTO_INCREMENT = 6
  DEFAULT CHARSET = utf8;

INSERT INTO `goods_stock`
VALUES ('1', 'iPhone 14 Pro', '50', '0', '0');
INSERT INTO `goods_stock`
VALUES ('2', 'MacBook Pro 14', '10', '0', '0');
INSERT INTO `goods_stock`
VALUES ('3', 'iPad Pro 11', '20', '0', '0');
INSERT INTO `goods_stock`
VALUES ('4', 'Apple Watch Series 8', '40', '0', '0');
INSERT INTO `goods_stock`
VALUES ('5', 'AirPods Pro 2', '30', '0', '0');

DROP TABLE IF EXISTS `goods_order`;
CREATE TABLE `goods_order`
(
    `order_id`    INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `goods_id`    INT(11)          NOT NULL COMMENT 'Goods ID',
    `goods_name`  VARCHAR(255)     NOT NULL DEFAULT '' COMMENT 'Goods name',
    `user_id`     INT(11)          NOT NULL DEFAULT '0',
    `create_time` TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`order_id`)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `user_id`  INT(11)      NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(255) NOT NULL DEFAULT '',
    PRIMARY KEY (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4;

INSERT INTO `user`
VALUES ('1', 'YG');