


import 'dart:math';

class RandomGuidId {
  static String generate() {
    var chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
    var random = Random.secure();
    var result = List.generate(10, (index) => chars[random.nextInt(chars.length)]).join();
    return result;
  }
}