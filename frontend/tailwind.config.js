/** @type {import('tailwindcss').Config} */
export default {
  darkMode: "class",
  content: ["./index.html", "./src/**/*.{js,ts,vue}"],
  theme: {
    container: {
      center: true,
      padding: '2rem',
      screens: {
        '2xl': '1400px',
      },
    },
    extend: {
      colors: {
        // Brand Colors
        primary: {
          DEFAULT: '#FF8DA1', // Peach Pink from Image 1
          50: '#FFF5F7',
          100: '#FFE6EB',
          200: '#FFCCD6',
          300: '#FFA3B5',
          400: '#FF8DA1',
          500: '#FF6B85',
          600: '#E64560',
        },
        // Secondary Accents
        secondary: {
          orange: '#FF9F43',
          green: '#2ED573',
          blue: '#54A0FF',
          yellow: '#FFD32A',
          purple: '#A55EEA',
        },
        // Backgrounds
        cream: {
          DEFAULT: '#FFF9F5', // Warm cream for Classroom
          100: '#FFF9F5',
          200: '#FDF2E9',
        },
        // Text
        dark: {
          900: '#2D3436',
          800: '#636E72',
          500: '#B2BEC3',
        },
        // Classroom Specific
        brown: {
          DEFAULT: '#5D4037', // Footer/Text
          100: '#8D6E63',
        }
      },
      fontFamily: {
        sans: ['"M PLUS Rounded 1c"', '"Nunito"', 'sans-serif'],
        serif: ['"Noto Serif SC"', 'serif'],
      },
      borderRadius: {
        'xl': '1rem',
        '2xl': '1.5rem',
        '3xl': '2rem',
        '4xl': '2.5rem',
      },
      boxShadow: {
        'soft': '0 10px 40px -10px rgba(0,0,0,0.05)',
        'card': '0 4px 20px -5px rgba(0,0,0,0.03)',
        'glow': '0 0 20px rgba(255, 141, 161, 0.3)',
      },
      backgroundImage: {
        'home-hero': 'linear-gradient(135deg, #FFF0F5 0%, #FFF5E6 100%)',
        'market-gradient': 'linear-gradient(180deg, #F3E5F5 0%, #FCE4EC 40%, #E3F2FD 80%, #FFF9C4 100%)',
        'classroom-hero': 'linear-gradient(120deg, #8E8E8E 0%, #AFAFAF 100%)', // Warm gray
        'contact-gradient': 'linear-gradient(90deg, #FF9A9E 0%, #FECFEF 99%, #FECFEF 100%)',
        'footer-dots': 'radial-gradient(#ffffff 2px, transparent 2px)',
      },
      backgroundSize: {
        'dots': '20px 20px',
      }
    },
  },
  plugins: [],
};