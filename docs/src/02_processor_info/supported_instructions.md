# Supported Instructions
The instruction set implemented in this processor was based on [this MIPS reference sheet](https://booksite.elsevier.com/9780124077263/downloads/COD_5e_Greencard.pdf).

*Note: This CPU currently does <u>NOT</u> support floating point operations.*

<table>
    <thead>
        <tr> <th>Non R-Type Instructions</th> <th>R-Type Instructions</th> </tr>
    </thead>
<tbody><tr>
    <td style="padding: 0 0; border: 0 white solid">
        <table>
            <thead>
                <tr> <th>OP</th> <th>Name</th> <th></th> <th>Note</th> </tr>
            </thead>
            <tbody>
                <tr> <td>0</td> <td style="white-space: nowrap">R-Type</td> <td>🟨</td> <td>See Table</td> </tr>
                <tr> <td>1</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>2</td> <td><code>j</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>3</td> <td><code>jal</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>4</td> <td><code>beq</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>5</td> <td><code>bne</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>6</td> <td><code>blez</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>7</td> <td><code>bgtz</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>8</td> <td><code>addi</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>9</td> <td><code>addiu</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>10</td> <td><code>slti</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>11</td> <td><code>sltiu</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>12</td> <td><code>andi</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>13</td> <td><code>ori</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>14</td> <td><code>xori</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>15</td> <td><code>lui</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>16</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>17</td> <td>F-Type</td> <td>🟥</td> <td style="white-space: nowrap">Not Yet Implemented</td> </tr>
                <tr> <td>18</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>19</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>20</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>21</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>22</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>23</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>24</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>25</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>26</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>27</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>28</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>29</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>30</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>31</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>32</td> <td><code>lb</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>33</td> <td><code>lh</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>34</td> <td><code>lwl</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>35</td> <td><code>lw</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>36</td> <td><code>lbu</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>37</td> <td><code>lhu</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>38</td> <td><code>lwr</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>39</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>40</td> <td><code>sb</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>41</td> <td><code>sh</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>42</td> <td><code>swl</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>43</td> <td><code>sw</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>44</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>45</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>46</td> <td><code>swr</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>47</td> <td><code>cache</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>48</td> <td><code>ll</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>49</td> <td><code>lwc1</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>50</td> <td><code>lwc2</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>51</td> <td><code>pref</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>52</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>53</td> <td><code>ldc1</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>54</td> <td><code>ldc2</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>55</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>56</td> <td><code>sc</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>57</td> <td><code>swc1</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>58</td> <td><code>swc2</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>59</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>60</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>61</td> <td><code>sdc1</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>62</td> <td><code>sdc2</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>63</td> <td></td> <td>⬛️</td> <td></td> </tr>
            </tbody>
        </table>
    </td>
    <td style="padding: 0 0; border: 0 white solid">
        <table>
            <thead>
                <tr> <th>Funct</th> <th>Name</th> <th></th> <th>Note</th> </tr>
            </thead>
            <tbody>
                <tr> <td>0</td> <td><code>sll</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>1</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>2</td> <td><code>srl</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>3</td> <td><code>sra</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>4</td> <td><code>sllv</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>5</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>6</td> <td><code>srlv</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>7</td> <td><code>srav</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>8</td> <td><code>jr</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>9</td> <td><code>jalr</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>10</td> <td><code>movz</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>11</td> <td><code>movn</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>12</td> <td><code>syscall</code></td> <td>🟩</td> <td>Implemented<a href="#footnote1">*</a> <!-- TODO: Add link for asterisk explaining inserted prior move instruction --> </td> </tr>
                <tr> <td>13</td> <td><code>break</code></td> <td>🟥</td> <td style="white-space: nowrap">Not Yet Implemented</td> </tr>
                <tr> <td>14</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>15</td> <td><code>sync</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>16</td> <td><code>mfhi</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>17</td> <td><code>mthi</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>18</td> <td><code>mflo</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>19</td> <td><code>mtlo</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>20</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>21</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>22</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>23</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>24</td> <td><code>mult</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>25</td> <td><code>multu</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>26</td> <td><code>div</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>27</td> <td><code>divu</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>28</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>29</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>30</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>31</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>32</td> <td><code>add</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>33</td> <td><code>addu</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>34</td> <td><code>sub</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>35</td> <td><code>subu</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>36</td> <td><code>and</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>37</td> <td><code>or</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>38</td> <td><code>xor</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>39</td> <td><code>nor</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>40</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>41</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>42</td> <td><code>slt</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>43</td> <td><code>sltu</code></td> <td>🟩</td> <td>Implemented</td> </tr>
                <tr> <td>44</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>45</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>46</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>47</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>48</td> <td><code>tge</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>49</td> <td><code>tgeu</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>50</td> <td><code>tlt</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>51</td> <td><code>tltu</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>52</td> <td><code>teq</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>53</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>54</td> <td><code>tne</code></td> <td>🟥</td> <td>Not Yet Implemented</td> </tr>
                <tr> <td>55</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>56</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>57</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>58</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>59</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>60</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>61</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>62</td> <td></td> <td>⬛️</td> <td></td> </tr>
                <tr> <td>63</td> <td></td> <td>⬛️</td> <td></td> </tr>
            </tbody>
        </table>
    </td>
</tr></tbody>
</table>

<p id="footnote1">
* The <code>syscall</code> instruction has a <code>move $k0, $ra</code> instruction inserted prior to it by the pre-processor.
This is done to allow for <code>syscall</code> to preserve the value in <code>$ra</code>, since <code>syscall</code>
acts similarly to a <code>jal</code> instruction; despite the convention placing the responsibility of preserving
the content of <code>$ra</code> on the callee.
<i>Future improvements may remove the need for this additional instruction's insertion.</i>
</p>