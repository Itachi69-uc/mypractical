#include<iostream>
#include<fstream>
using namespace std;

class Employee {
    int code;
    char name[20];
    float salary;
public:
    void read() {
        cout << "Enter employee code: "; cin >> code;
        cout << "Enter name: "; cin.ignore(); cin.getline(name, 20);
        cout << "Enter salary: "; cin >> salary;
    }
    void display() { cout << code << " " << name << "\t" << salary << endl; }
    int getEmpCode() { return code; }
    float getSalary() { return salary; }
    void updateSalary(float s) { salary = s; }
};

fstream file;

void appendToFile() {
    Employee x; x.read();
    file.open("EMPLOYEE.DAT", ios::binary | ios::app);
    if (!file) { cout << "ERROR IN CREATING FILE\n"; return; }
    file.write((char*)&x, sizeof(x)); file.close();
    cout << "Record added successfully.\n";
}

void displayAll() {
    Employee x;
    file.open("EMPLOYEE.DAT", ios::binary | ios::in);
    if (!file) { cout << "ERROR IN OPENING FILE\n"; return; }
    while (file.read((char*)&x, sizeof(x)))
        if (x.getSalary() >= 10000 && x.getSalary() <= 20000) x.display();
    file.close();
}

void searchForRecord() {
    Employee x; int c, found = 0;
    cout << "Enter employee code: "; cin >> c;
    file.open("EMPLOYEE.DAT", ios::binary | ios::in);
    if (!file) { cout << "ERROR IN OPENING FILE\n"; return; }
    while (file.read((char*)&x, sizeof(x))) {
        if (x.getEmpCode() == c) { cout << "RECORD FOUND\n"; x.display(); found = 1; break; }
    }
    if (!found) cout << "Record not found!!!\n";
    file.close();
}

void increaseSalary() {
    Employee x; int c, found = 0; float sal;
    cout << "Enter employee code: "; cin >> c;
    file.open("EMPLOYEE.DAT", ios::binary | ios::in | ios::out);
    if (!file) { cout << "ERROR IN OPENING FILE\n"; return; }
    while (file.read((char*)&x, sizeof(x))) {
        if (x.getEmpCode() == c) {
            cout << "Salary hike? "; cin >> sal;
            x.updateSalary(x.getSalary() + sal);
            file.seekp((int)file.tellg() - sizeof(x));
            file.write((char*)&x, sizeof(x));
            cout << "Salary updated successfully.\n"; found = 1; break;
        }
    }
    if (!found) cout << "Record not found!!!\n";
    file.close();
}

void insertRecord() {
    Employee x, newEmp; newEmp.read();
    fstream temp("TEMP.DAT", ios::binary | ios::out);
    file.open("EMPLOYEE.DAT", ios::binary | ios::in);
    if (!file || !temp) { cout << "ERROR IN OPENING FILE\n"; return; }
    bool inserted = false;
    while (file.read((char*)&x, sizeof(x))) {
        if (!inserted && x.getEmpCode() > newEmp.getEmpCode()) {
            temp.write((char*)&newEmp, sizeof(newEmp));
            inserted = true;
        }
        temp.write((char*)&x, sizeof(x));
    }
    if (!inserted) temp.write((char*)&newEmp, sizeof(newEmp));
    file.close(); temp.close();
    remove("EMPLOYEE.DAT"); rename("TEMP.DAT", "EMPLOYEE.DAT");
    cout << "Record inserted successfully.\n";
}

int main() {
    remove("EMPLOYEE.DAT");
    char ch;
    do {
        int n;
        cout << "ENTER CHOICE\n1.ADD\n2.DISPLAY\n3.SEARCH\n4.INCREASE SALARY\n5.INSERT\nChoice: ";
        cin >> n;
        switch (n) {
            case 1: appendToFile(); break;
            case 2: displayAll(); break;
            case 3: searchForRecord(); break;
            case 4: increaseSalary(); break;
            case 5: insertRecord(); break;
            default: cout << "Invalid Choice\n";
        }
        cout << "Do you want to continue? (y/n): "; cin >> ch;
    } while (ch == 'y' || ch == 'Y');
    return 0;
}

