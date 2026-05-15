#![allow(unused_mut)]

use std::io;

fn read_i32() -> i32 {
    let mut s = String::new();
    io::stdin().read_line(&mut s).unwrap();
    s.trim().parse().unwrap()
}

fn main() {
let mut x: i32;
x = read_i32();
println!("{}", x);
}
