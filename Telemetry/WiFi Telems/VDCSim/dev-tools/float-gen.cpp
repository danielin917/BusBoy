#include <iostream>
#include <iomanip>

using namespace std;

int main()
{
	cout << sizeof(short) << endl << sizeof(float) << endl << sizeof(int) << endl;

	unsigned short f[8];
	f[0] = 84;
	f[1] = 5;
	f[2] = 9980;
	f[3] = 0;
	f[4] = 0;
	f[5] = 0;
	f[6] = 0;
	f[7] = 0;

	for(int i = 7; i >= 0; i--)
		cout << hex << setfill('0') << setw(2) << (int) ((unsigned char*)f)[i] ;
	cout << endl;
	cout << setw(8) << ((int*)f)[0] ;
	cout << setw(8) << ((int*)f)[1] << endl;
}
