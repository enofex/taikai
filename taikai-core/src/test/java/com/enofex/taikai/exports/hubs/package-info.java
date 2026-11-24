/**
 * Fixtures of the hubs: {@code First} and {@code Second} use {@code Hub} and {@code Router},
 * {@code Third} only {@code Hub}, and {@code Hub} and {@code Router} both use {@code Left} and
 * {@code Right}. So {@code Hub} has a fan-in of 3 and {@code Router} of 2, both with a fan-out of 2,
 * while no class uses {@code First} and {@code Second}, and {@code Left} and {@code Right} use no
 * class.
 */
package com.enofex.taikai.exports.hubs;
