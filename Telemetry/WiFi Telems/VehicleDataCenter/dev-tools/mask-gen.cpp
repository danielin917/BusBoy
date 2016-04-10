#include <iostream>
#include <iomanip>

using namespace std;

int main()
{
	long l = 1;
	for(int i = 0; i< 64; i++, l = l*=2)
		cout << "0x" << hex << l-1 << "L, "  << endl;

}
