using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using Solar;

public partial class Form1 : Form
{
    public Form1()
    {
        InitializeComponent();
        DataBus databus = new DataBus();
        databus.subscribeRecords(CallbackRecordFunction);
        databus.subscribeAll(CallbackValueFunction);
        databus.connect("localhost", 10000);
    }
    void CallbackValueFunction(Solar.Message message)
    {
        this.textBox2.Text = message.ToString();
        this.textBox4.Text = Convert.ToString(1 + Convert.ToInt32(this.textBox4.Text));
    }
    void CallbackRecordFunction(Solar.RecordDescriptor recordDescriptor)
    {
        this.textBox1.Text = recordDescriptor.ToString();
        this.textBox3.Text = Convert.ToString(1 + Convert.ToInt32(this.textBox3.Text));
    }
}
