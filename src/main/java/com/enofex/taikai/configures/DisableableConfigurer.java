package com.enofex.taikai.configures;

public interface DisableableConfigurer extends Configurer {

  <T extends Configurer> T disable();

}
