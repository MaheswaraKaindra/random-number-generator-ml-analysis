import numpy as np
import os
from tqdm import tqdm
from numpy.lib.stride_tricks import sliding_window_view

def load_and_convert_to_bits(filepath):

    print(f"Reading {filepath}...")

    with open(filepath, 'r') as f:
        lines = [line.strip() for line in f if line.strip()]
    
    print("Converting decimal to 32-bit binary format...")
    bit_strings = [format(int(num), '032b') for num in tqdm(lines)]
    
    full_bit_string = ''.join(bit_strings)
    bit_array = np.array([int(b) for b in full_bit_string], dtype=np.int8)
    
    return bit_array

def create_dataset_windows(bit_array, window_size):

    print(f"Creating sliding windows with size {window_size} bits...")
    windows = sliding_window_view(bit_array, window_size + 1)
    
    # Pisahkan X dan y
    X = windows[:, :-1]
    y = windows[:, -1]
    
    return X, y

def preprocess(input_file, output_prefix, window_size=64):

    if not os.path.exists(input_file):
        print(f"File not found: {input_file}")
        return

    # 1. Convert
    bit_array = load_and_convert_to_bits(input_file)
    print(f"Total: {len(bit_array):,} bits")
    
    # 2. Windowing
    X, y = create_dataset_windows(bit_array, window_size)
    print(f"Shape X: {X.shape}")
    print(f"Shape y: {y.shape}")
    
    # .npz
    output_path = f"../data/processed/{output_prefix}_w{window_size}.npz"
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    print(f"Saving to {output_path}...")
    np.savez_compressed(output_path, X=X, y=y)
    print("Done.\n")

if __name__ == "__main__":
    WINDOW_SIZE = 64

    datasets = [
        ("../../data/raw/lcg_data.txt", "lcg"),
        ("../../data/raw/xorshift_data.txt", "xorshift"),
        ("../../data/raw/a51_data.txt", "a51"),
        ("../../data/raw/chacha20_data.txt", "chacha20"),
        ("../../data/raw/securerandom_data.txt", "securerandom")
    ]
    
    for input_file, prefix in datasets:
        preprocess(input_file, prefix, WINDOW_SIZE)