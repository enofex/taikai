/**
 * Fixtures of the exports: {@code app} uses {@code api} and {@code domain}, {@code api} and
 * {@code spi} use {@code domain}, which is stable and concrete, and {@code billing} and
 * {@code payment} depend on each other in a cycle. {@code payment} also uses the less stable
 * {@code app}. {@code app} uses Spring, {@code billing} Jakarta Validation, and {@code spi} only a
 * class of the JDK outside of {@code java}. Nothing uses {@code spi}, which is abstract.
 */
package com.enofex.taikai.exports.fixtures;
