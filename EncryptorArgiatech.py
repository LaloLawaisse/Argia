import tkinter as tk
from tkinter import filedialog
from tink import cleartext_keyset_handle
from tink import _keyset_reader
from tink import streaming_aead
from tink.streaming_aead import streaming_aead_key_templates

class Encryptor:
    def __init__(self):
        streaming_aead.register()
        key_template = streaming_aead_key_templates.AES128_CTR_HMAC_SHA256_4KB
        keyset_reader = _keyset_reader.JsonKeysetReader(get_key())
        self.keyset_handle = cleartext_keyset_handle.read(keyset_reader)
        self.streaming_aead = self.keyset_handle.primitive(streaming_aead.StreamingAead)
        self.aad = bytes([2, 50, 34, 17, 36, 67, 121, 21, 69, 35, 2, 86, 68, 19, 85, 85])  

    def encrypt(self, input_file, output_file=None):
        with open(input_file, "rb") as f:
            with open(output_file, "wb") as encrypted_f:
                encrypting_channel = self.streaming_aead.new_encrypting_stream(encrypted_f, self.aad)
                encrypting_channel.write(f.read())

    def decrypt(self, input_file, output_file=None):
        with open(input_file, "rb") as encrypted_f:
            with open(output_file, "wb") as f:
                decrypting_channel = self.streaming_aead.new_decrypting_stream(encrypted_f, self.aad)
                f.write(decrypting_channel.read())
def get_key():
    return '''
    {
        "primaryKeyId": 1658370469,
        "key": [{
            "keyData": {
                "typeUrl": "type.googleapis.com/google.crypto.tink.AesCtrHmacStreamingKey",
                "keyMaterialType": "SYMMETRIC",
                "value": "Eg0IgCAQEBgDIgQIAxAgGhCLR6mbaYWa6ZoNGF3/qpZr"
            },
            "outputPrefixType": "RAW",
            "keyId": 1658370469,
            "status": "ENABLED"
        }]
    }
    '''

class EncryptorGUI:
    def __init__(self, master):
        self.master = master
        master.title("Rec ARGIATECH Ejercicio")

        self.encryptor = Encryptor()

        self.label = tk.Label(master, text="Elija una accion:")
        self.label.pack()

        self.encrypt_button = tk.Button(master, text="Encriptar", command=self.encrypt)
        self.encrypt_button.pack()

        self.decrypt_button = tk.Button(master, text="Desencriptar", command=self.decrypt)
        self.decrypt_button.pack()

    def encrypt(self):
        file_path = filedialog.askopenfilename(title="Elija archivo para encriptar")
        if file_path:
            output_file = filedialog.asksaveasfilename(defaultextension=".mp4", filetypes=(("Archivos Encriptados", "*.mp4"), ("Todos los archivos", "*.*")))
            if output_file:
                self.encryptor.encrypt(file_path, output_file)
                tk.messagebox.showinfo("Exito", "Archivo encriptado correctamente!")

    def decrypt(self):
        file_path = filedialog.askopenfilename(title="Elija archivo para desencriptar")
        if file_path:
            output_file = filedialog.asksaveasfilename(defaultextension=".mp4", filetypes=(("Archivos Desncriptados", "*.mp4"), ("Todos los archivos", "*.*")))
            if output_file:
                self.encryptor.decrypt(file_path, output_file)
                tk.messagebox.showinfo("Exito", "Archivo desencriptado correctamente!")

# Create the Tkinter window
root = tk.Tk()
my_gui = EncryptorGUI(root)
root.mainloop()
