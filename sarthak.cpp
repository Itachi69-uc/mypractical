#include<iostream>
#include<fstream>
#include<cstring>
using namespace std;

class Student {
public:
    int roll;
    char name[20], div, address[30];
    void input() {
        cout << "\nRoll: "; cin >> roll;
        cout << "Name: "; cin >> name;
        cout << "Div: "; cin >> div;
        cout << "Address: "; cin >> address;
    }
    void display() {
        cout << roll << "\t" << name << "\t" << div << "\t" << address << endl;
    }
};

int main() {
    Student s;
    fstream f;
    int ch, key, found;
    char nkey[20];

    do {
        cout << "\n1.Insert 2.Show 3.Edit by Roll 4.Edit by Name 5.Delete 6.Exit: ";
        cin >> ch;
        switch(ch) {
            case 1:
                f.open("Stu.txt", ios::app|ios::binary);
                s.input();
                f.write((char*)&s, sizeof(s));
                f.close();
                break;
            case 2:
                f.open("Stu.txt", ios::in|ios::binary);
                while(f.read((char*)&s, sizeof(s)))
                    s.display();
                f.close();
                break;
            case 3:
                cout << "Roll to edit: "; cin >> key;
                f.open("Stu.txt", ios::in|ios::out|ios::binary);
                found = 0;
                while(f.read((char*)&s, sizeof(s))) {
                    if(s.roll == key) {
                        f.seekp(-sizeof(s), ios::cur);
                        s.input();
                        f.write((char*)&s, sizeof(s));
                        found = 1;
                        break;
                    }
                }
                if(!found) cout << "Not found\n";
                f.close();
                break;
            case 4:
                cout << "Name to edit: "; cin >> nkey;
                f.open("Stu.txt", ios::in|ios::out|ios::binary);
                found = 0;
                while(f.read((char*)&s, sizeof(s))) {
                    if(strcmp(s.name, nkey) == 0) {
                        f.seekp(-sizeof(s), ios::cur);
                        s.input();
                        f.write((char*)&s, sizeof(s));
                        found = 1;
                        break;
                    }
                }
                if(!found) cout << "Not found\n";
                f.close();
                break;
            case 5:
                cout << "Roll to delete: "; cin >> key;
                {
                    fstream temp;
                    f.open("Stu.txt", ios::in|ios::binary);
                    temp.open("Temp.txt", ios::out|ios::binary);
                    while(f.read((char*)&s, sizeof(s))) {
                        if(s.roll != key)
                            temp.write((char*)&s, sizeof(s));
                    }
                    f.close();
                    temp.close();
                    remove("Stu.txt");
                    rename("Temp.txt", "Stu.txt");
                }
                break;
        }
    } while(ch != 6);
}
