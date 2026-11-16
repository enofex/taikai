/**
 * Fixtures of the dependencies on less stable packages: {@code web} and {@code batch} use
 * {@code core}, which uses {@code helper}, which uses {@code text}. With {@code core} at an
 * instability of 0.33 and {@code helper} at 0.50, {@code core} depends on a less stable package.
 */
package com.enofex.taikai.exports.stability;
